package com.recruiting.platform.domain.model;

import java.io.Serializable;
import java.util.Map;

public record NormalizedAgentRequest(
        String requestId,
        String tenantId,
        String sessionId,
        String userId,
        String query,
        String channel,
        Map<String, Object> metadata
) implements Serializable {
}
