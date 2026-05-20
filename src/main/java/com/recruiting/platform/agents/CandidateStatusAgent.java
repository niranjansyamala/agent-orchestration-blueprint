package com.recruiting.platform.agents;

import com.recruiting.platform.domain.model.AgentExecutionContext;
import com.recruiting.platform.domain.model.AgentExecutionResult;
import com.recruiting.platform.tools.RecruitingToolsService;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    public AgentExecutionResult execute(AgentExecutionContext context) {
        String candidateId = String.valueOf(context.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        String newStatus = String.valueOf(context.request().metadata().getOrDefault("newStatus", "IN_REVIEW"));
        String reason = String.valueOf(context.request().metadata().getOrDefault("statusReason", "Updated by recruiting workflow"));

        Map<String, Object> durableUpdate = toolsService.updateCandidateStatus(
                context.request().tenantId(),
                candidateId,
                newStatus,
                context.request().userId(),
                reason
        );

        return new AgentExecutionResult(
                "success",
                agentId(),
                "candidate_status_updated",
                candidateId,
                newStatus,
                "",
                durableUpdate
        );
    }
}
