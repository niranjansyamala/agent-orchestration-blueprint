package com.recruiting.platform.common.model;

import java.io.Serializable;

public record ExecuteAgentRequest(
        String workflowId,
        String targetAgentId,
        NormalizedAgentRequest request,
        RoutingDecision routingDecision
) implements Serializable {
}
