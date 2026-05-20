package com.recruiting.platform.execution.service;

import com.recruiting.platform.common.model.AgentExecutionResult;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.execution.agent.RecruitingAgent;
import com.recruiting.platform.execution.toolregistry.ToolSelectionPlan;
import com.recruiting.platform.execution.toolregistry.ToolSelectionService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AgentExecutionRuntime {

    private final Map<String, RecruitingAgent> agentsById;
    private final ToolSelectionService toolSelectionService;

    public AgentExecutionRuntime(List<RecruitingAgent> agents,
                                 ToolSelectionService toolSelectionService) {
        this.agentsById = agents.stream().collect(Collectors.toMap(RecruitingAgent::agentId, Function.identity()));
        this.toolSelectionService = toolSelectionService;
    }

    public AgentExecutionResult execute(ExecuteAgentRequest request) {
        RecruitingAgent agent = agentsById.get(request.targetAgentId());
        if (agent == null) {
            throw new IllegalArgumentException("No execution agent registered for " + request.targetAgentId());
        }
        ToolSelectionPlan selectionPlan = toolSelectionService.selectFor(request);
        AgentExecutionContext context = new AgentExecutionContext(request.workflowId(), selectionPlan);
        AgentExecutionResult result = agent.execute(request, context);
        return withSelectionMetadata(result, selectionPlan);
    }

    private AgentExecutionResult withSelectionMetadata(AgentExecutionResult result, ToolSelectionPlan selectionPlan) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (result.payload() != null) {
            payload.putAll(result.payload());
        }
        payload.put("selectedTools", selectionPlan.selectedTools());
        payload.put("toolSelectionScores", selectionPlan.toolScores());
        return new AgentExecutionResult(
                result.status(),
                result.agentId(),
                result.actionTaken(),
                result.recordId(),
                result.newStatus(),
                result.auditRef(),
                payload
        );
    }
}
