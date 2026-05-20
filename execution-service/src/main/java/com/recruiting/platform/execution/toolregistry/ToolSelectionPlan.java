package com.recruiting.platform.execution.toolregistry;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ToolSelectionPlan(
        String targetAgentId,
        List<String> selectedTools,
        Map<String, Integer> toolScores,
        Map<String, String> toolRationales
) {

    public boolean allows(String toolName) {
        return selectedTools.contains(toolName);
    }

    public Set<String> selectedToolSet() {
        return Set.copyOf(selectedTools);
    }
}
