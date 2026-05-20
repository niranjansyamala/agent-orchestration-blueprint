package com.recruiting.platform.common.dto;

import java.io.Serializable;

public record AgentRequestAcceptedResponse(
        String requestId,
        String workflowId,
        String status
) implements Serializable {
}
