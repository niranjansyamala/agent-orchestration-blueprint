package com.recruiting.platform.application.dto;

import java.io.Serializable;

public record AgentRequestAcceptedResponse(
        String requestId,
        String workflowId,
        String status
) implements Serializable {
}
