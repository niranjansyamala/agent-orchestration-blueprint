package com.recruiting.platform.agents;

import com.recruiting.platform.ai.AiModelFactory;
import com.recruiting.platform.domain.model.AgentExecutionContext;
import com.recruiting.platform.domain.model.AgentExecutionResult;
import com.recruiting.platform.tools.RecruitingToolsService;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RecruiterCopilotAgent implements RecruitingAgent {

    private final AiModelFactory aiModelFactory;
    private final RecruitingToolsService toolsService;

    public RecruiterCopilotAgent(AiModelFactory aiModelFactory, RecruitingToolsService toolsService) {
        this.aiModelFactory = aiModelFactory;
        this.toolsService = toolsService;
    }

    @Override
    public String agentId() {
        return "recruiter_copilot_agent";
    }

    @Override
    public AgentExecutionResult execute(AgentExecutionContext context) {
        String candidateId = String.valueOf(context.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        String requisitionId = String.valueOf(context.request().metadata().getOrDefault("requisitionId", "req-42"));
        String contextBlob = toolsService.candidateProfile(candidateId) + "\n" + toolsService.requisitionSummary(requisitionId);

        String brief = aiModelFactory.chatModel()
                .map(model -> AiServices.builder(RecruitingCopilotAssistant.class)
                        .chatModel(model)
                        .build()
                        .recruiterBrief(context.request().query(), contextBlob))
                .orElse("Recruiting brief: review candidate fit, confirm interview readiness, and align next-step ownership.");

        return new AgentExecutionResult(
                "success",
                agentId(),
                "recruiter_brief_generated",
                candidateId,
                "NO_STATUS_CHANGE",
                "",
                Map.of(
                        "candidateId", candidateId,
                        "requisitionId", requisitionId,
                        "brief", brief
                )
        );
    }
}
