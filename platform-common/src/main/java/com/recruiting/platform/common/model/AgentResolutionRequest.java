package com.recruiting.platform.common.model;

import java.io.Serializable;
import java.util.List;

public record AgentResolutionRequest(
        String tenantId,
        String intent,
        List<String> requiredCapabilities
) implements Serializable {
}
