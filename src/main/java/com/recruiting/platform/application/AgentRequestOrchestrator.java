package com.recruiting.platform.application;

import com.recruiting.platform.application.dto.AgentRequestAcceptedResponse;
import com.recruiting.platform.application.dto.AgentRequestStatusResponse;
import com.recruiting.platform.application.dto.PauseWorkflowRequest;
import com.recruiting.platform.application.dto.ResumeWorkflowRequest;
import com.recruiting.platform.application.dto.SubmitAgentRequest;
import com.recruiting.platform.domain.WorkflowStatus;
import com.recruiting.platform.domain.model.NormalizedAgentRequest;
import com.recruiting.platform.persistence.entity.HitlTaskEntity;
import com.recruiting.platform.persistence.entity.WorkflowExecutionEntity;
import com.recruiting.platform.persistence.repository.HitlTaskRepository;
import com.recruiting.platform.persistence.repository.WorkflowExecutionRepository;
import com.recruiting.platform.support.Ids;
import com.recruiting.platform.support.JsonSupport;
import com.recruiting.platform.workflow.RecruitingWorkflowEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class AgentRequestOrchestrator {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final HitlTaskRepository hitlTaskRepository;
    private final RecruitingWorkflowEngine workflowEngine;
    private final JsonSupport jsonSupport;

    public AgentRequestOrchestrator(WorkflowExecutionRepository workflowExecutionRepository,
                                    HitlTaskRepository hitlTaskRepository,
                                    RecruitingWorkflowEngine workflowEngine,
                                    JsonSupport jsonSupport) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.hitlTaskRepository = hitlTaskRepository;
        this.workflowEngine = workflowEngine;
        this.jsonSupport = jsonSupport;
    }

    @Transactional
    public AgentRequestAcceptedResponse submit(String tenantId,
                                               String sessionId,
                                               String requestId,
                                               String userId,
                                               SubmitAgentRequest request) {
        String effectiveRequestId = requestId == null || requestId.isBlank() ? Ids.requestId() : requestId;
        String workflowId = Ids.workflowId();

        NormalizedAgentRequest normalizedRequest = new NormalizedAgentRequest(
                effectiveRequestId,
                tenantId,
                sessionId,
                userId == null ? "system-user" : userId,
                request.query(),
                request.channel(),
                request.metadata()
        );

        WorkflowExecutionEntity entity = new WorkflowExecutionEntity();
        entity.setWorkflowId(workflowId);
        entity.setRequestId(effectiveRequestId);
        entity.setTenantId(tenantId);
        entity.setSessionId(sessionId);
        entity.setUserId(normalizedRequest.userId());
        entity.setStatus(WorkflowStatus.ACCEPTED.name());
        entity.setCurrentStep("accepted");
        entity.setStartedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        workflowExecutionRepository.save(entity);

        WorkflowExecutionEntity finalState = workflowEngine.run(normalizedRequest, workflowId);
        return new AgentRequestAcceptedResponse(finalState.getRequestId(), finalState.getWorkflowId(), finalState.getStatus());
    }

    @Transactional(readOnly = true)
    public AgentRequestStatusResponse getStatus(String requestId) {
        WorkflowExecutionEntity entity = workflowExecutionRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("No workflow found for request " + requestId));
        return toStatusResponse(entity);
    }

    @Transactional
    public AgentRequestStatusResponse pause(String workflowId, PauseWorkflowRequest request) {
        WorkflowExecutionEntity workflow = workflowExecutionRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));
        workflow.setStatus(WorkflowStatus.PAUSED.name());
        workflow.setCurrentStep("paused");
        workflow.setUpdatedAt(Instant.now());
        workflowExecutionRepository.save(workflow);
        return toStatusResponse(workflow);
    }

    @Transactional
    public AgentRequestStatusResponse resume(String workflowId, ResumeWorkflowRequest request) {
        WorkflowExecutionEntity workflow = workflowExecutionRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));
        HitlTaskEntity hitlTask = hitlTaskRepository.findByWorkflowId(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("No HITL task found for workflow " + workflowId));

        hitlTask.setStatus(request.approved() ? "APPROVED" : "REJECTED");
        hitlTask.setAssignedTo(request.approvedBy());
        hitlTask.setApprovalPayload(jsonSupport.write(Map.of(
                "approved", request.approved(),
                "approvedBy", request.approvedBy()
        )));
        hitlTask.setResolvedAt(Instant.now());
        hitlTaskRepository.save(hitlTask);

        workflow.setStatus(request.approved() ? WorkflowStatus.COMPLETED.name() : WorkflowStatus.FAILED.name());
        workflow.setCurrentStep("resume");
        workflow.setCompletedAt(Instant.now());
        workflow.setUpdatedAt(Instant.now());
        workflow.setResultPayload(jsonSupport.write(Map.of(
                "approvalDecision", request.approved(),
                "approvedBy", request.approvedBy(),
                "message", request.approved() ? "Offer approval completed" : "Offer approval rejected"
        )));
        if (!request.approved()) {
            workflow.setErrorCode("APPROVAL_REJECTED");
            workflow.setErrorMessage("Human reviewer rejected the approval request");
        }
        workflowExecutionRepository.save(workflow);
        return toStatusResponse(workflow);
    }

    private AgentRequestStatusResponse toStatusResponse(WorkflowExecutionEntity entity) {
        return new AgentRequestStatusResponse(
                entity.getRequestId(),
                entity.getWorkflowId(),
                entity.getStatus(),
                entity.getTargetAgentId(),
                jsonSupport.readMap(entity.getResultPayload()),
                entity.getErrorCode(),
                entity.getErrorMessage()
        );
    }
}
