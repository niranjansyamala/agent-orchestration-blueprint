package com.recruiting.platform.execution.service;

import com.recruiting.platform.execution.toolregistry.ToolSelectionPlan;

public record AgentExecutionContext(
        String workflowId,
        ToolSelectionPlan toolSelectionPlan
) {
}
