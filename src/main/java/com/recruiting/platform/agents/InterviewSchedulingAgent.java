package com.recruiting.platform.agents;

import com.recruiting.platform.domain.model.AgentExecutionContext;
import com.recruiting.platform.domain.model.AgentExecutionResult;
import com.recruiting.platform.tools.RecruitingToolsService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InterviewSchedulingAgent implements RecruitingAgent {

    private final RecruitingToolsService toolsService;

    public InterviewSchedulingAgent(RecruitingToolsService toolsService) {
        this.toolsService = toolsService;
    }

    @Override
    public String agentId() {
        return "interview_scheduling_agent";
    }

    @Override
    public AgentExecutionResult execute(AgentExecutionContext context) {
        String candidateId = String.valueOf(context.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        String panelName = String.valueOf(context.request().metadata().getOrDefault("panelName", "Engineering Panel"));
        List<String> slots = toolsService.suggestInterviewSlots(candidateId, panelName);
        toolsService.updateCandidateStatus(
                context.request().tenantId(),
                candidateId,
                "INTERVIEW_SCHEDULED",
                context.request().userId(),
                "Interview slots proposed"
        );

        return new AgentExecutionResult(
                "success",
                agentId(),
                "interview_scheduled",
                candidateId,
                "INTERVIEW_SCHEDULED",
                "",
                Map.of(
                        "candidateId", candidateId,
                        "panelName", panelName,
                        "suggestedSlots", slots
                )
        );
    }
}
