package com.recruiting.platform.common.dto;

import java.io.Serializable;
import java.util.Map;

public record AgentRequestStatusResponse(
        String requestId,
        String workflowId,
        String status,
        String targetAgentId,
        Map<String, Object> result,
        String errorCode,
        String errorMessage
) implements Serializable {
}
