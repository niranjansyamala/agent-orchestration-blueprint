package com.recruiting.platform.common.model;

import java.io.Serializable;
import java.util.Map;

public record AgentExecutionResult(
        String status,
        String agentId,
        String actionTaken,
        String recordId,
        String newStatus,
        String auditRef,
        Map<String, Object> payload
) implements Serializable {
}
