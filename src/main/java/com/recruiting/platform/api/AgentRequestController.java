package com.recruiting.platform.api;

import com.recruiting.platform.application.AgentRequestOrchestrator;
import com.recruiting.platform.application.dto.AgentRequestAcceptedResponse;
import com.recruiting.platform.application.dto.AgentRequestStatusResponse;
import com.recruiting.platform.application.dto.PauseWorkflowRequest;
import com.recruiting.platform.application.dto.ResumeWorkflowRequest;
import com.recruiting.platform.application.dto.SubmitAgentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class AgentRequestController {

    private final AgentRequestOrchestrator orchestrator;

    public AgentRequestController(AgentRequestOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/api/v1/agent-requests")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AgentRequestAcceptedResponse submit(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody SubmitAgentRequest request) {
        return orchestrator.submit(tenantId, sessionId, requestId, userId, request);
    }

    @GetMapping("/api/v1/agent-requests/{requestId}")
    public AgentRequestStatusResponse getStatus(@PathVariable String requestId) {
        return orchestrator.getStatus(requestId);
    }

    @PostMapping("/api/v1/workflows/{workflowId}/pause")
    public AgentRequestStatusResponse pause(@PathVariable String workflowId,
                                            @Valid @RequestBody PauseWorkflowRequest request) {
        return orchestrator.pause(workflowId, request);
    }

    @PostMapping("/api/v1/workflows/{workflowId}/resume")
    public AgentRequestStatusResponse resume(@PathVariable String workflowId,
                                             @Valid @RequestBody ResumeWorkflowRequest request) {
        return orchestrator.resume(workflowId, request);
    }
}
