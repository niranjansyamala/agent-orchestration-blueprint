package com.recruiting.platform.agents;

import com.recruiting.platform.domain.model.AgentExecutionContext;
import com.recruiting.platform.domain.model.AgentExecutionResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OfferApprovalAgent implements RecruitingAgent {

    @Override
    public String agentId() {
        return "offer_approval_agent";
    }

    @Override
    public AgentExecutionResult execute(AgentExecutionContext context) {
        String candidateId = String.valueOf(context.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        return new AgentExecutionResult(
                "success",
                agentId(),
                "offer_approval_requested",
                candidateId,
                "PENDING_APPROVAL",
                "",
                Map.of(
                        "candidateId", candidateId,
                        "message", "Offer approval requires human review before final status change"
                )
        );
    }
}
