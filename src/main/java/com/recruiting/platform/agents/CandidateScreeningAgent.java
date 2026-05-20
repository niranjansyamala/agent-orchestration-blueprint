package com.recruiting.platform.agents;

import com.recruiting.platform.ai.AiModelFactory;
import com.recruiting.platform.domain.model.AgentExecutionContext;
import com.recruiting.platform.domain.model.AgentExecutionResult;
import com.recruiting.platform.tools.RecruitingToolsService;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CandidateScreeningAgent implements RecruitingAgent {

    private final AiModelFactory aiModelFactory;
    private final RecruitingToolsService toolsService;

    public CandidateScreeningAgent(AiModelFactory aiModelFactory, RecruitingToolsService toolsService) {
        this.aiModelFactory = aiModelFactory;
        this.toolsService = toolsService;
    }

    @Override
    public String agentId() {
        return "candidate_screening_agent";
    }

    @Override
    public AgentExecutionResult execute(AgentExecutionContext context) {
        String candidateId = String.valueOf(context.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        String requisitionId = String.valueOf(context.request().metadata().getOrDefault("requisitionId", "req-42"));
        String candidateProfile = toolsService.candidateProfile(candidateId);
        String requisitionSummary = toolsService.requisitionSummary(requisitionId);

        String summary = aiModelFactory.chatModel()
                .map(model -> AiServices.builder(RecruitingCopilotAssistant.class)
                        .chatModel(model)
                        .build()
                        .screeningSummary(candidateProfile, requisitionSummary))
                .orElse("""
                        Strong fit for the requisition based on Java platform depth, stakeholder communication,
                        and workflow automation experience. Recommend recruiter screen followed by panel interview.
                        """.trim());

        return new AgentExecutionResult(
                "success",
                agentId(),
                "candidate_screened",
                candidateId,
                "SCREENED",
                "",
                Map.of(
                        "candidateId", candidateId,
                        "requisitionId", requisitionId,
                        "summary", summary
                )
        );
    }
}
