package com.recruiting.platform.execution.agent;

import com.recruiting.platform.common.model.AgentExecutionResult;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.execution.service.AgentExecutionContext;
import com.recruiting.platform.execution.tools.RecruitingToolsService;
import org.springframework.stereotype.Component;

@Component
public class CandidateStatusAgent implements RecruitingAgent {

    private final RecruitingToolsService toolsService;

    public CandidateStatusAgent(RecruitingToolsService toolsService) {
        this.toolsService = toolsService;
    }

    @Override
    public String agentId() {
        return "candidate_status_agent";
    }

    @Override
    public AgentExecutionResult execute(ExecuteAgentRequest request, AgentExecutionContext context) {
        String candidateId = String.valueOf(request.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        String newStatus = String.valueOf(request.request().metadata().getOrDefault("newStatus", "IN_REVIEW"));
        String reason = String.valueOf(request.request().metadata().getOrDefault("statusReason", "Updated by recruiting workflow"));
        var payload = toolsService.updateCandidateStatus(context, request.request().tenantId(), candidateId,
                newStatus, request.request().userId(), reason);
        return new AgentExecutionResult("success", agentId(), "candidate_status_updated", candidateId, newStatus, "", payload);
    }
}
