package com.recruiting.platform.execution;

import com.recruiting.platform.agents.RecruitingAgent;
import com.recruiting.platform.domain.model.AgentExecutionContext;
import com.recruiting.platform.domain.model.AgentExecutionResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AgentExecutionRuntime {

    private final Map<String, RecruitingAgent> agentsById;

    public AgentExecutionRuntime(List<RecruitingAgent> agents) {
        this.agentsById = agents.stream().collect(Collectors.toMap(RecruitingAgent::agentId, Function.identity()));
    }

    public AgentExecutionResult execute(String agentId, AgentExecutionContext context) {
        RecruitingAgent agent = agentsById.get(agentId);
        if (agent == null) {
            throw new IllegalStateException("No agent registered for id " + agentId);
        }
        return agent.execute(context);
    }
}
