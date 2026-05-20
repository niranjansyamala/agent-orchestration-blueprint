package com.recruiting.platform.execution.agent;

import com.recruiting.platform.common.model.AgentExecutionResult;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.execution.service.AgentExecutionContext;
import com.recruiting.platform.execution.tools.RecruitingToolsService;
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
    public AgentExecutionResult execute(ExecuteAgentRequest request, AgentExecutionContext context) {
        String candidateId = String.valueOf(request.request().metadata().getOrDefault("candidateId", "candidate-1001"));
        String panelName = String.valueOf(request.request().metadata().getOrDefault("panelName", "Engineering Panel"));
        List<String> slots = toolsService.suggestInterviewSlots(context, candidateId, panelName);
        toolsService.updateCandidateStatus(context, request.request().tenantId(), candidateId,
                "INTERVIEW_SCHEDULED", request.request().userId(), "Interview slots proposed");
        return new AgentExecutionResult("success", agentId(), "interview_scheduled", candidateId,
                "INTERVIEW_SCHEDULED", "", Map.of("candidateId", candidateId, "panelName", panelName, "suggestedSlots", slots));
    }
}
