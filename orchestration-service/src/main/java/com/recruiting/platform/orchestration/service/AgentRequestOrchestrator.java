package com.recruiting.platform.orchestration.service;

import com.recruiting.platform.common.domain.WorkflowStatus;
import com.recruiting.platform.common.dto.AgentRequestAcceptedResponse;
import com.recruiting.platform.common.dto.AgentRequestStatusResponse;
import com.recruiting.platform.common.dto.PauseWorkflowRequest;
import com.recruiting.platform.common.dto.ResumeWorkflowRequest;
import com.recruiting.platform.common.dto.SubmitAgentRequest;
import com.recruiting.platform.common.model.NormalizedAgentRequest;
import com.recruiting.platform.common.support.Ids;
import com.recruiting.platform.common.support.JsonSupport;
import com.recruiting.platform.orchestration.persistence.entity.HitlTaskEntity;
import com.recruiting.platform.orchestration.persistence.entity.WorkflowExecutionEntity;
import com.recruiting.platform.orchestration.persistence.repository.HitlTaskRepository;
import com.recruiting.platform.orchestration.persistence.repository.WorkflowExecutionRepository;
import com.recruiting.platform.orchestration.workflow.RecruitingWorkflowEngine;
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
    public AgentRequestAcceptedResponse submit(String tenantId, String sessionId, String requestId, String userId, SubmitAgentRequest request) {
        String effectiveRequestId = (requestId == null || requestId.isBlank()) ? Ids.requestId() : requestId;
        String workflowId = Ids.workflowId();
        NormalizedAgentRequest normalized = new NormalizedAgentRequest(
                effectiveRequestId, tenantId, sessionId, userId == null ? "system-user" : userId,
                request.query(), request.channel(), request.metadata()
        );
        WorkflowExecutionEntity entity = new WorkflowExecutionEntity();
        entity.setWorkflowId(workflowId);
        entity.setRequestId(effectiveRequestId);
        entity.setTenantId(tenantId);
        entity.setSessionId(sessionId);
        entity.setUserId(normalized.userId());
        entity.setStatus(WorkflowStatus.ACCEPTED.name());
        entity.setCurrentStep("accepted");
        entity.setStartedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        workflowExecutionRepository.save(entity);

        WorkflowExecutionEntity finalState = workflowEngine.run(normalized, workflowId);
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
        WorkflowExecutionEntity entity = workflowExecutionRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));
        entity.setStatus(WorkflowStatus.PAUSED.name());
        entity.setCurrentStep("paused");
        entity.setUpdatedAt(Instant.now());
        workflowExecutionRepository.save(entity);
        return toStatusResponse(entity);
    }

    @Transactional
    public AgentRequestStatusResponse resume(String workflowId, ResumeWorkflowRequest request) {
        WorkflowExecutionEntity entity = workflowExecutionRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));
        HitlTaskEntity hitlTask = hitlTaskRepository.findByWorkflowId(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("No HITL task found for workflow " + workflowId));
        hitlTask.setStatus(request.approved() ? "APPROVED" : "REJECTED");
        hitlTask.setAssignedTo(request.approvedBy());
        hitlTask.setApprovalPayload(jsonSupport.write(Map.of("approved", request.approved(), "approvedBy", request.approvedBy())));
        hitlTask.setResolvedAt(Instant.now());
        hitlTaskRepository.save(hitlTask);

        entity.setStatus(request.approved() ? WorkflowStatus.COMPLETED.name() : WorkflowStatus.FAILED.name());
        entity.setCurrentStep("resume");
        entity.setCompletedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setResultPayload(jsonSupport.write(Map.of(
                "approvalDecision", request.approved(),
                "approvedBy", request.approvedBy(),
                "message", request.approved() ? "Offer approval completed" : "Offer approval rejected"
        )));
        if (!request.approved()) {
            entity.setErrorCode("APPROVAL_REJECTED");
            entity.setErrorMessage("Human reviewer rejected the approval request");
        }
        workflowExecutionRepository.save(entity);
        return toStatusResponse(entity);
    }

    private AgentRequestStatusResponse toStatusResponse(WorkflowExecutionEntity entity) {
        return new AgentRequestStatusResponse(entity.getRequestId(), entity.getWorkflowId(), entity.getStatus(),
                entity.getTargetAgentId(), jsonSupport.readMap(entity.getResultPayload()), entity.getErrorCode(), entity.getErrorMessage());
    }
}
