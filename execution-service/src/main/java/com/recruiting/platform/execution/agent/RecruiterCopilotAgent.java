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
    public AgentExecutionResult execute(ExecuteAgentRequest request, AgentExecutionContext context) {
        String candidateId = String.valueOf(request.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        String requisitionId = String.valueOf(request.request().metadata().getOrDefault("requisitionId", "req-42"));
        String contextBlob = toolsService.candidateProfile(context, candidateId) + "\n"
                + toolsService.requisitionSummary(context, requisitionId);

        String brief = aiModelFactory.chatModel()
                .map(model -> AiServices.builder(RecruitingCopilotAssistant.class)
                        .chatModel(model)
                        .build()
                        .recruiterBrief(request.request().query(), contextBlob))
                .orElse("Recruiting brief: review candidate fit, confirm interview readiness, and align next-step ownership.");

        return new AgentExecutionResult("success", agentId(), "recruiter_brief_generated", candidateId,
                "NO_STATUS_CHANGE", "", Map.of("candidateId", candidateId, "requisitionId", requisitionId, "brief", brief));
    }
}
