package com.recruiting.platform.workflow;

import com.recruiting.platform.domain.WorkflowStatus;
import com.recruiting.platform.domain.model.AgentExecutionContext;
import com.recruiting.platform.domain.model.AgentExecutionResult;
import com.recruiting.platform.domain.model.NormalizedAgentRequest;
import com.recruiting.platform.domain.model.RoutingDecision;
import com.recruiting.platform.execution.AgentExecutionRuntime;
import com.recruiting.platform.memory.RedisStateStore;
import com.recruiting.platform.observability.LangSmithTracingService;
import com.recruiting.platform.persistence.entity.AuditEventEntity;
import com.recruiting.platform.persistence.entity.HitlTaskEntity;
import com.recruiting.platform.persistence.entity.WorkflowExecutionEntity;
import com.recruiting.platform.persistence.entity.WorkflowStepExecutionEntity;
import com.recruiting.platform.persistence.repository.AuditEventRepository;
import com.recruiting.platform.persistence.repository.HitlTaskRepository;
import com.recruiting.platform.persistence.repository.WorkflowExecutionRepository;
import com.recruiting.platform.persistence.repository.WorkflowStepExecutionRepository;
import com.recruiting.platform.registry.AgentRegistryResolver;
import com.recruiting.platform.registry.ResolvedAgent;
import com.recruiting.platform.router.RecruitingRouter;
import com.recruiting.platform.support.Ids;
import com.recruiting.platform.support.JsonSupport;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
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
    private final AgentRegistryResolver registryResolver;
    private final AgentExecutionRuntime executionRuntime;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowStepExecutionRepository workflowStepExecutionRepository;
    private final AuditEventRepository auditEventRepository;
    private final HitlTaskRepository hitlTaskRepository;
    private final RedisStateStore redisStateStore;
    private final JsonSupport jsonSupport;
    private final LangSmithTracingService tracingService;
    private final CompiledGraph<RecruitingWorkflowState> compiledGraph;

    public RecruitingWorkflowEngine(RecruitingRouter router,
                                    AgentRegistryResolver registryResolver,
                                    AgentExecutionRuntime executionRuntime,
                                    WorkflowExecutionRepository workflowExecutionRepository,
                                    WorkflowStepExecutionRepository workflowStepExecutionRepository,
                                    AuditEventRepository auditEventRepository,
                                    HitlTaskRepository hitlTaskRepository,
                                    RedisStateStore redisStateStore,
                                    JsonSupport jsonSupport,
                                    LangSmithTracingService tracingService) throws Exception {
        this.router = router;
        this.registryResolver = registryResolver;
        this.executionRuntime = executionRuntime;
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
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(KEY_REQUEST, request);
        initialState.put(KEY_WORKFLOW_ID, workflowId);

        RunnableConfig config = RunnableConfig.builder()
                .threadId(workflowId)
                .build();

        Optional<RecruitingWorkflowState> result = compiledGraph.invoke(initialState, config);
        if (result.isEmpty()) {
            throw new IllegalStateException("Workflow returned no final state");
        }
        return workflowExecutionRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalStateException("Workflow state missing for " + workflowId));
    }

    private CompiledGraph<RecruitingWorkflowState> buildGraph() throws Exception {
        StateGraph<RecruitingWorkflowState> graph = new StateGraph<>(RecruitingWorkflowState::new);
        graph.addNode("route", (state, config) -> CompletableFuture.completedFuture(routeNode(state)));
        graph.addNode("resolveAgent", (state, config) -> CompletableFuture.completedFuture(resolveNode(state)));
        graph.addNode("executeAgent", (state, config) -> CompletableFuture.completedFuture(executeNode(state)));
        graph.addNode("persistOutcome", (state, config) -> CompletableFuture.completedFuture(persistOutcomeNode(state)));
        graph.addNode("requestApproval", (state, config) -> CompletableFuture.completedFuture(requestApprovalNode(state)));
        graph.addNode("finalizeFailure", (state, config) -> CompletableFuture.completedFuture(finalizeFailureNode(state)));

        graph.addEdge(GraphDefinition.START, "route");
        graph.addEdge("route", "resolveAgent");
        graph.addConditionalEdges("resolveAgent", this::afterResolve, Map.of(
                "execute", "executeAgent",
                "approval", "requestApproval",
                "failed", "finalizeFailure"
        ));
        graph.addConditionalEdges("executeAgent", this::afterExecute, Map.of(
                "persist", "persistOutcome",
                "failed", "finalizeFailure"
        ));
        graph.addEdge("persistOutcome", GraphDefinition.END);
        graph.addEdge("requestApproval", GraphDefinition.END);
        graph.addEdge("finalizeFailure", GraphDefinition.END);

        return graph.compile(CompileConfig.builder()
                .graphId("recruiting-workflow")
                .checkpointSaver(new MemorySaver())
                .build());
    }

    private Map<String, Object> routeNode(RecruitingWorkflowState state) {
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        return tracingService.inSpan("workflow.route",
                Map.of("workflow.id", workflowId, "tenant.id", request.tenantId()),
                () -> {
                    updateWorkflow(workflowId, WorkflowStatus.ROUTING, "route", null, null, null);
                    RoutingDecision routingDecision = router.route(request);
                    recordStep(workflowId, "route", "router", "completed", Map.of("query", request.query()),
                            Map.of("intent", routingDecision.intent().name(), "confidence", routingDecision.confidence()),
                            null, null);
                    redisStateStore.saveSession(request, workflowId, routingDecision.intent().name());
                    return Map.of(KEY_ROUTING, routingDecision);
                });
    }

    private Map<String, Object> resolveNode(RecruitingWorkflowState state) {
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        RoutingDecision routingDecision = state.value(KEY_ROUTING, (RoutingDecision) null);

        Optional<ResolvedAgent> resolved = registryResolver.resolve(request.tenantId(), routingDecision);
        if (resolved.isEmpty()) {
            updateWorkflow(workflowId, WorkflowStatus.FAILED, "resolveAgent", null, "NO_AGENT_MATCH",
                    "No active agent matched routing decision");
            recordStep(workflowId, "resolveAgent", "registry", "failed", Map.of("intent", routingDecision.intent().name()),
                    Map.of(), "NO_AGENT_MATCH", "No active agent matched routing decision");
            return Map.of(
                    KEY_ERROR_CODE, "NO_AGENT_MATCH",
                    KEY_ERROR_MESSAGE, "No active agent matched routing decision"
            );
        }

        ResolvedAgent resolvedAgent = resolved.get();
        updateWorkflow(workflowId,
                routingDecision.humanReviewRequired() ? WorkflowStatus.WAITING_FOR_APPROVAL : WorkflowStatus.EXECUTING,
                "resolveAgent",
                resolvedAgent.agentId(),
                null,
                null);
        recordStep(workflowId, "resolveAgent", "registry", "completed", Map.of("intent", routingDecision.intent().name()),
                Map.of("agentId", resolvedAgent.agentId(), "dispatchTarget", resolvedAgent.dispatchTarget()),
                null, null);
        return Map.of(KEY_AGENT, resolvedAgent);
    }

    private CompletableFuture<String> afterResolve(RecruitingWorkflowState state) {
        if (state.value(KEY_ERROR_CODE).isPresent()) {
            return CompletableFuture.completedFuture("failed");
        }
        RoutingDecision routingDecision = state.value(KEY_ROUTING, (RoutingDecision) null);
        return CompletableFuture.completedFuture(routingDecision.humanReviewRequired() ? "approval" : "execute");
    }

    private Map<String, Object> executeNode(RecruitingWorkflowState state) {
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        RoutingDecision routingDecision = state.value(KEY_ROUTING, (RoutingDecision) null);
        ResolvedAgent resolvedAgent = state.value(KEY_AGENT, (ResolvedAgent) null);
        String workflowId = state.value(KEY_WORKFLOW_ID, "");

        return tracingService.inSpan("workflow.execute",
                Map.of("workflow.id", workflowId, "agent.id", resolvedAgent.agentId()),
                () -> {
                    AgentExecutionContext context = new AgentExecutionContext(workflowId, request, routingDecision);
                    AgentExecutionResult result = executionRuntime.execute(resolvedAgent.agentId(), context);
                    recordStep(workflowId, "executeAgent", "agent", "completed",
                            Map.of("agentId", resolvedAgent.agentId()),
                            result.payload(),
                            null,
                            null);
                    return Map.of(KEY_EXECUTION_RESULT, result);
                });
    }

    private CompletableFuture<String> afterExecute(RecruitingWorkflowState state) {
        return CompletableFuture.completedFuture(state.value(KEY_ERROR_CODE).isPresent() ? "failed" : "persist");
    }

    private Map<String, Object> persistOutcomeNode(RecruitingWorkflowState state) {
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        RoutingDecision routingDecision = state.value(KEY_ROUTING, (RoutingDecision) null);
        ResolvedAgent resolvedAgent = state.value(KEY_AGENT, (ResolvedAgent) null);
        AgentExecutionResult result = state.value(KEY_EXECUTION_RESULT, (AgentExecutionResult) null);

        updateWorkflow(workflowId, WorkflowStatus.COMPLETED, "persistOutcome", resolvedAgent.agentId(), null, null);
        writeAudit(request, workflowId, resolvedAgent.agentId(), "WORKFLOW_COMPLETED",
                Map.of("intent", routingDecision.intent().name(), "confidence", routingDecision.confidence()),
                result.payload());
        redisStateStore.saveWorkflowState(workflowId, Map.of(
                "status", WorkflowStatus.COMPLETED.name(),
                "agentId", resolvedAgent.agentId(),
                "result", result.payload()
        ));
        return Map.of();
    }

    private Map<String, Object> requestApprovalNode(RecruitingWorkflowState state) {
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        RoutingDecision routingDecision = state.value(KEY_ROUTING, (RoutingDecision) null);
        ResolvedAgent resolvedAgent = state.value(KEY_AGENT, (ResolvedAgent) null);

        HitlTaskEntity hitlTaskEntity = new HitlTaskEntity();
        hitlTaskEntity.setHitlTaskId(Ids.hitlId());
        hitlTaskEntity.setWorkflowId(workflowId);
        hitlTaskEntity.setTenantId(request.tenantId());
        hitlTaskEntity.setTaskType(routingDecision.intent().name());
        hitlTaskEntity.setStatus("WAITING_APPROVAL");
        hitlTaskEntity.setApprovalPayload(jsonSupport.write(Map.of(
                "query", request.query(),
                "candidateId", request.metadata().getOrDefault("candidateId", "candidate-1001"),
                "requestedAgent", resolvedAgent.agentId()
        )));
        hitlTaskEntity.setCreatedAt(Instant.now());
        hitlTaskRepository.save(hitlTaskEntity);

        writeAudit(request, workflowId, resolvedAgent.agentId(), "HITL_REQUIRED",
                Map.of("intent", routingDecision.intent().name()),
                Map.of("taskId", hitlTaskEntity.getHitlTaskId()));
        redisStateStore.saveWorkflowState(workflowId, Map.of(
                "status", WorkflowStatus.WAITING_FOR_APPROVAL.name(),
                "hitlTaskId", hitlTaskEntity.getHitlTaskId()
        ));
        return Map.of();
    }

    private Map<String, Object> finalizeFailureNode(RecruitingWorkflowState state) {
        String workflowId = state.value(KEY_WORKFLOW_ID, "");
        NormalizedAgentRequest request = state.value(KEY_REQUEST, (NormalizedAgentRequest) null);
        String errorCode = state.value(KEY_ERROR_CODE, "");
        String errorMessage = state.value(KEY_ERROR_MESSAGE, "");

        updateWorkflow(workflowId, WorkflowStatus.FAILED, "finalizeFailure", null, errorCode, errorMessage);
        writeAudit(request, workflowId, null, "WORKFLOW_FAILED",
                Map.of("errorCode", errorCode),
                Map.of("errorMessage", errorMessage));
        redisStateStore.saveWorkflowState(workflowId, Map.of(
                "status", WorkflowStatus.FAILED.name(),
                "errorCode", errorCode,
                "errorMessage", errorMessage
        ));
        return Map.of();
    }

    private void updateWorkflow(String workflowId,
                                WorkflowStatus status,
                                String currentStep,
                                String targetAgentId,
                                String errorCode,
                                String errorMessage) {
        WorkflowExecutionEntity entity = workflowExecutionRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalStateException("Workflow not found: " + workflowId));
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

    private void recordStep(String workflowId,
                            String stepName,
                            String stepType,
                            String status,
                            Map<String, Object> input,
                            Map<String, Object> output,
                            String errorCode,
                            String errorMessage) {
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

    private void writeAudit(NormalizedAgentRequest request,
                            String workflowId,
                            String agentId,
                            String eventType,
                            Map<String, Object> decisionSummary,
                            Map<String, Object> resultPayload) {
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
