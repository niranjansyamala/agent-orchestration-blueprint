package com.recruiting.platform.common.model;

import com.recruiting.platform.common.domain.DispatchType;

import java.io.Serializable;
import java.util.List;

public record ResolvedAgent(
        String agentId,
        String version,
        DispatchType dispatchType,
        String dispatchTarget,
        int priority,
        String healthStatus,
        List<String> supportedIntents,
        List<String> capabilities
) implements Serializable {
}
