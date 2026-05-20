package com.recruiting.platform.execution.agent;

import com.recruiting.platform.common.model.AgentExecutionResult;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.execution.ai.AiModelFactory;
import com.recruiting.platform.execution.service.AgentExecutionContext;
import com.recruiting.platform.execution.tools.RecruitingToolsService;
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
    public AgentExecutionResult execute(ExecuteAgentRequest request, AgentExecutionContext context) {
        String candidateId = String.valueOf(request.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        String requisitionId = String.valueOf(request.request().metadata().getOrDefault("requisitionId", "req-42"));
        String candidateProfile = toolsService.candidateProfile(context, candidateId);
        String requisitionSummary = toolsService.requisitionSummary(context, requisitionId);

        String summary = aiModelFactory.chatModel()
                .map(model -> AiServices.builder(RecruitingCopilotAssistant.class)
                        .chatModel(model)
                        .build()
                        .screeningSummary(candidateProfile, requisitionSummary))
                .orElse("""
                        Strong fit for the requisition based on Java platform depth, stakeholder communication,
                        and workflow automation experience. Recommend recruiter screen followed by panel interview.
                        """.trim());

        return new AgentExecutionResult("success", agentId(), "candidate_screened", candidateId, "SCREENED", "",
                Map.of("candidateId", candidateId, "requisitionId", requisitionId, "summary", summary));
    }
}
