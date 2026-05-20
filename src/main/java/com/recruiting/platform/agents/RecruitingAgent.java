package com.recruiting.platform.agents;

import com.recruiting.platform.domain.model.AgentExecutionContext;
import com.recruiting.platform.domain.model.AgentExecutionResult;

public interface RecruitingAgent {

    String agentId();

    AgentExecutionResult execute(AgentExecutionContext context);
}
