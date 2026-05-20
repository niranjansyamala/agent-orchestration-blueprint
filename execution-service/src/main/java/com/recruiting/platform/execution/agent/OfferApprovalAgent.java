package com.recruiting.platform.execution.agent;

import com.recruiting.platform.common.model.AgentExecutionResult;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.execution.service.AgentExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OfferApprovalAgent implements RecruitingAgent {

    @Override
    public String agentId() {
        return "offer_approval_agent";
    }

    @Override
    public AgentExecutionResult execute(ExecuteAgentRequest request, AgentExecutionContext context) {
        String candidateId = String.valueOf(request.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        return new AgentExecutionResult("success", agentId(), "offer_approval_requested", candidateId,
                "PENDING_APPROVAL", "", Map.of("candidateId", candidateId,
                "message", "Offer approval requires human review before final status change"));
    }
}
