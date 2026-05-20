package com.recruiting.platform.domain.model;

import java.io.Serializable;

public record AgentExecutionContext(
        String workflowId,
        NormalizedAgentRequest request,
        RoutingDecision routingDecision
) implements Serializable {
}
