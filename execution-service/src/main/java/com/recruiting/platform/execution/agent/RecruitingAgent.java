package com.recruiting.platform.execution.agent;

import com.recruiting.platform.common.model.AgentExecutionResult;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.execution.service.AgentExecutionContext;

public interface RecruitingAgent {
    String agentId();
    AgentExecutionResult execute(ExecuteAgentRequest request, AgentExecutionContext context);
}
