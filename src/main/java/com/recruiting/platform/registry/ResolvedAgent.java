package com.recruiting.platform.registry;

import com.recruiting.platform.domain.DispatchType;

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
