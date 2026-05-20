package com.recruiting.platform.orchestration.workflow;

import com.recruiting.platform.common.domain.WorkflowStatus;
import com.recruiting.platform.common.model.AgentExecutionResult;
import com.recruiting.platform.common.model.AgentResolutionRequest;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.common.model.NormalizedAgentRequest;
import com.recruiting.platform.common.model.ResolvedAgent;
import com.recruiting.platform.common.model.RoutingDecision;
import com.recruiting.platform.common.support.Ids;
import com.recruiting.platform.common.support.JsonSupport;
import com.recruiting.platform.orchestration.client.ExecutionServiceClient;
import com.recruiting.platform.orchestration.client.RegistryServiceClient;
import com.recruiting.platform.orchestration.memory.RedisStateStore;
import com.recruiting.platform.orchestration.observability.LangSmithTracingService;
import com.recruiting.platform.orchestration.persistence.entity.AuditEventEntity;
import com.recruiting.platform.orchestration.persistence.entity.HitlTaskEntity;
import com.recruiting.platform.orchestration.persistence.entity.WorkflowExecutionEntity;
import com.recruiting.platform.orchestration.persistence.entity.WorkflowStepExecutionEntity;
import com.recruiting.platform.orchestration.persistence.repository.AuditEventRepository;
import com.recruiting.platform.orchestration.persistence.repository.HitlTaskRepository;
import com.recruiting.platform.orchestration.persistence.repository.WorkflowExecutionRepository;
import com.recruiting.platform.orchestration.persistence.repository.WorkflowStepExecutionRepository;
import com.recruiting.platform.orchestration.router.RecruitingRouter;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class RecruitingWorkflowEngine {

    private static final String KEY_REQUEST = "request";
    private static final String KEY_WORKFLOW_ID = "workflowId";
    private static final String KEY_ROUTING = "routingDecision";
    private static final String KEY_AGENT = "resolvedAgent";
    private static final String KEY_EXECUTION_RESULT = "executionResult";
    private static final String KEY_ERROR_CODE = "errorCode";
    private static final String KEY_ERROR_MESSAGE = "errorMessage";

    private final RecruitingRouter router;
    private final RegistryServiceClient registryClient;
    private final ExecutionServiceClient executionClient;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowStepExecutionRepository workflowStepExecutionRepository;
    private final AuditEventRepository auditEventRepository;
    private final HitlTaskRepository hitlTaskRepository;
    private final RedisStateStore redisStateStore;
    private final JsonSupport jsonSupport;
    private final LangSmithTracingService tracingService;
    private final CompiledGraph<RecruitingWorkflowState> compiledGraph;

    public RecruitingWorkflowEngine(RecruitingRouter router,
                                    RegistryServiceClient registryClient,
                                    ExecutionServiceClient executionClient,
                                    WorkflowExecutionRepository workflowExecutionRepository,
                                    WorkflowStepExecutionRepository workflowStepExecutionRepository,
                                    AuditEventRepository auditEventRepository,
                                    HitlTaskRepository hitlTaskRepository,
                                    RedisStateStore redisStateStore,
                                    JsonSupport jsonSupport,
                                    LangSmithTracingService tracingService) throws Exception {
        this.router = router;
        this.registryClient = registryClient;
        this.executionClient = executionClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowStepExecutionRepository = workflowStepExecutionRepository;
        this.auditEventRepository = auditEventRepository;
        this.hitlTaskRepository = hitlTaskRepository;
        this.redisStateStore = redisStateStore;
        this.jsonSupport = jsonSupport;
        this.tracingService = tracingService;
        this.compiledGraph = buildGraph();
    }

    public WorkflowExecutionEntity run(NormalizedAgentRequest request, String workflowId) {
        RunnableConfig config = RunnableConfig.builder().threadId(workflowId).build();
        Optional<RecruitingWorkflowState> state = compiledGraph.invoke(Map.of(KEY_REQUEST, request, KEY_WORKFLOW_ID, workflowId), config);
        if (state.isEmpty()) {
            throw new IllegalStateException("Workflow returned no final state");
        }
        return workflowExecutionRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalStateException("Workflow state missing for " + workflowId));
    }

    private CompiledGraph<RecruitingWorkflowState> buildGraph() throws Exception {
        StateGraph<RecruitingWorkflowState> graph = new StateGraph<>(RecruitingWorkflowState::new);
        graph.addNode("route", (state, cfg) -> CompletableFuture.completedFuture(routeNode(state)));
        graph.addNode("resolveAgent", (state, cfg) -> CompletableFuture.completedFuture(resolveNode(state)));
        graph.addNode("executeAgent", (state, cfg) -> CompletableFuture.completedFuture(executeNode(state)));
        graph.addNode("persistOutcome", (state, cfg) -> CompletableFuture.completedFuture(persistNode(state)));
        graph.addNode("requestApproval", (state, cfg) -> CompletableFuture.completedFuture(requestApprovalNode(state)));
        graph.addNode("finalizeFailure", (state, cfg) -> CompletableFuture.completedFuture(failureNode(state)));

        graph.addEdge(GraphDefinition.START, "route");
        graph.addEdge("route", "resolveAgent");
        graph.addConditionalEdges("resolveAgent", this::afterResolve, Map.of("execute", "executeAgent", "approval", "requestApproval", "failed", "finalizeFailure"));
        graph.addConditionalEdges("executeAgent", this::afterExecute, Map.of("persist", "persistOutcome", "failed", "finalizeFailure"));
        graph.addEdge("persistOutcome", GraphDefinition.END);
        graph.addEdge("requestApproval", GraphDefinition.END);
        graph.addEdge("finalizeFailure", GraphDefinition.END);

        return graph.compile(CompileConfig.builder().graphId("recruiting-workflow").checkpointSaver(new MemorySaver()).build());
    }

    private Map<String, Object> routeNode(RecruitingWorkflowState state) {
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        return tracingService.inSpan("workflow.route", Map.of("workflow.id", workflowId, "tenant.id", request.tenantId()), () -> {
            updateWorkflow(workflowId, WorkflowStatus.ROUTING, "route", null, null, null);
            RoutingDecision decision = router.route(request);
            updateRouteMetadata(workflowId, decision);
            recordStep(workflowId, "route", "router", "completed", Map.of("query", request.query()),
                    Map.of("intent", decision.intent().name(), "confidence", decision.confidence()), null, null);
            redisStateStore.saveSession(request, workflowId, decision.intent().name());
            return Map.of(KEY_ROUTING, decision);
        });
    }

    private Map<String, Object> resolveNode(RecruitingWorkflowState state) {
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        RoutingDecision routingDecision = state.value(KEY_ROUTING, (RoutingDecision) null);
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        try {
            ResolvedAgent resolvedAgent = registryClient.resolve(new AgentResolutionRequest(
                    request.tenantId(),
                    routingDecision.intent().name(),
                    routingDecision.requiredCapabilities()
            ));
            updateWorkflow(workflowId,
                    routingDecision.humanReviewRequired() ? WorkflowStatus.WAITING_FOR_APPROVAL : WorkflowStatus.EXECUTING,
                    "resolveAgent", resolvedAgent.agentId(), null, null);
            recordStep(workflowId, "resolveAgent", "registry", "completed",
                    Map.of("intent", routingDecision.intent().name()),
                    Map.of("agentId", resolvedAgent.agentId()), null, null);
            return Map.of(KEY_AGENT, resolvedAgent);
        } catch (RuntimeException ex) {
            updateWorkflow(workflowId, WorkflowStatus.FAILED, "resolveAgent", null, "NO_AGENT_MATCH", ex.getMessage());
            return Map.of(KEY_ERROR_CODE, "NO_AGENT_MATCH", KEY_ERROR_MESSAGE, ex.getMessage());
        }
    }

    private CompletableFuture<String> afterResolve(RecruitingWorkflowState state) {
        if (state.value(KEY_ERROR_CODE).isPresent()) {
            return CompletableFuture.completedFuture("failed");
        }
        RoutingDecision decision = state.value(KEY_ROUTING, (RoutingDecision) null);
        return CompletableFuture.completedFuture(decision.humanReviewRequired() ? "approval" : "execute");
    }

    private Map<String, Object> executeNode(RecruitingWorkflowState state) {
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        RoutingDecision decision = state.value(KEY_ROUTING, (RoutingDecision) null);
        ResolvedAgent agent = state.value(KEY_AGENT, (ResolvedAgent) null);
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        try {
            AgentExecutionResult result = executionClient.execute(new ExecuteAgentRequest(workflowId, agent.agentId(), request, decision));
            recordStep(workflowId, "executeAgent", "execution-service", "completed", Map.of("agentId", agent.agentId()), result.payload(), null, null);
            return Map.of(KEY_EXECUTION_RESULT, result);
        } catch (RuntimeException ex) {
            return Map.of(KEY_ERROR_CODE, "EXECUTION_FAILED", KEY_ERROR_MESSAGE, ex.getMessage());
        }
    }

    private CompletableFuture<String> afterExecute(RecruitingWorkflowState state) {
        return CompletableFuture.completedFuture(state.value(KEY_ERROR_CODE).isPresent() ? "failed" : "persist");
    }

    private Map<String, Object> persistNode(RecruitingWorkflowState state) {
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        ResolvedAgent agent = state.value(KEY_AGENT, (ResolvedAgent) null);
        AgentExecutionResult result = state.value(KEY_EXECUTION_RESULT, (AgentExecutionResult) null);
        updateWorkflow(workflowId, WorkflowStatus.COMPLETED, "persistOutcome", agent.agentId(), null, null);
        WorkflowExecutionEntity entity = workflowExecutionRepository.findById(workflowId).orElseThrow();
        entity.setResultPayload(jsonSupport.write(result.payload()));
        entity.setUpdatedAt(Instant.now());
        workflowExecutionRepository.save(entity);
        writeAudit(request, workflowId, agent.agentId(), "WORKFLOW_COMPLETED", Map.of("status", result.status()), result.payload());
        redisStateStore.saveWorkflowState(workflowId, Map.of("status", WorkflowStatus.COMPLETED.name(), "agentId", agent.agentId(), "result", result.payload()));
        return Map.of();
    }

    private Map<String, Object> requestApprovalNode(RecruitingWorkflowState state) {
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        RoutingDecision decision = state.value(KEY_ROUTING, (RoutingDecision) null);
        ResolvedAgent agent = state.value(KEY_AGENT, (ResolvedAgent) null);
        HitlTaskEntity hitl = new HitlTaskEntity();
        hitl.setHitlTaskId(Ids.hitlId());
        hitl.setWorkflowId(workflowId);
        hitl.setTenantId(request.tenantId());
        hitl.setTaskType(decision.intent().name());
        hitl.setStatus("WAITING_APPROVAL");
        hitl.setApprovalPayload(jsonSupport.write(Map.of("query", request.query(), "requestedAgent", agent.agentId())));
        hitl.setCreatedAt(Instant.now());
        hitlTaskRepository.save(hitl);
        writeAudit(request, workflowId, agent.agentId(), "HITL_REQUIRED", Map.of("intent", decision.intent().name()), Map.of("taskId", hitl.getHitlTaskId()));
        redisStateStore.saveWorkflowState(workflowId, Map.of("status", WorkflowStatus.WAITING_FOR_APPROVAL.name(), "hitlTaskId", hitl.getHitlTaskId()));
        return Map.of();
    }

    private Map<String, Object> failureNode(RecruitingWorkflowState state) {
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        String errorCode = state.value(KEY_ERROR_CODE, "");
        String errorMessage = state.value(KEY_ERROR_MESSAGE, "");
        updateWorkflow(workflowId, WorkflowStatus.FAILED, "finalizeFailure", null, errorCode, errorMessage);
        writeAudit(request, workflowId, null, "WORKFLOW_FAILED", Map.of("errorCode", errorCode), Map.of("errorMessage", errorMessage));
        redisStateStore.saveWorkflowState(workflowId, Map.of("status", WorkflowStatus.FAILED.name(), "errorCode", errorCode, "errorMessage", errorMessage));
        return Map.of();
    }

    private void updateRouteMetadata(String workflowId, RoutingDecision decision) {
        WorkflowExecutionEntity entity = workflowExecutionRepository.findById(workflowId).orElseThrow();
        entity.setIntent(decision.intent().name());
        entity.setRouterConfidence(decision.confidence());
        entity.setUpdatedAt(Instant.now());
        workflowExecutionRepository.save(entity);
    }

    private void updateWorkflow(String workflowId, WorkflowStatus status, String currentStep, String targetAgentId, String errorCode, String errorMessage) {
        WorkflowExecutionEntity entity = workflowExecutionRepository.findById(workflowId).orElseThrow();
        entity.setStatus(status.name());
        entity.setCurrentStep(currentStep);
        if (targetAgentId != null) {
            entity.setTargetAgentId(targetAgentId);
        }
        entity.setErrorCode(errorCode);
        entity.setErrorMessage(errorMessage);
        entity.setUpdatedAt(Instant.now());
        if (status == WorkflowStatus.COMPLETED || status == WorkflowStatus.FAILED) {
            entity.setCompletedAt(Instant.now());
        }
        workflowExecutionRepository.save(entity);
    }

    private void recordStep(String workflowId, String stepName, String stepType, String status,
                            Map<String, Object> input, Map<String, Object> output, String errorCode, String errorMessage) {
        WorkflowStepExecutionEntity entity = new WorkflowStepExecutionEntity();
        entity.setStepExecutionId(Ids.stepId());
        entity.setWorkflowId(workflowId);
        entity.setStepName(stepName);
        entity.setStepType(stepType);
        entity.setStatus(status);
        entity.setAttemptNo(1);
        entity.setInputPayload(jsonSupport.write(input));
        entity.setOutputPayload(jsonSupport.write(output));
        entity.setStartedAt(Instant.now());
        entity.setCompletedAt(Instant.now());
        entity.setErrorCode(errorCode);
        entity.setErrorMessage(errorMessage);
        workflowStepExecutionRepository.save(entity);
    }

    private void writeAudit(NormalizedAgentRequest request, String workflowId, String agentId, String eventType,
                            Map<String, Object> decisionSummary, Map<String, Object> resultPayload) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setAuditEventId(Ids.auditId());
        entity.setRequestId(request.requestId());
        entity.setWorkflowId(workflowId);
        entity.setTenantId(request.tenantId());
        entity.setAgentId(agentId);
        entity.setEventType(eventType);
        entity.setEventTimestamp(Instant.now());
        entity.setActorType("SYSTEM");
        entity.setActorId(request.userId());
        entity.setInputContext(jsonSupport.write(Map.of("query", request.query(), "metadata", request.metadata())));
        entity.setDecisionSummary(jsonSupport.write(decisionSummary));
        entity.setResultPayload(jsonSupport.write(resultPayload));
        auditEventRepository.save(entity);
    }
}
