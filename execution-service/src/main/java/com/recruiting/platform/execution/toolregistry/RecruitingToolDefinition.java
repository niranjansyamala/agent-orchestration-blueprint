package com.recruiting.platform.execution.toolregistry;

import java.util.List;
import java.util.Set;

public record RecruitingToolDefinition(
        String toolName,
        String domain,
        String description,
        ToolSource source,
        Set<String> capabilities,
        Set<String> requiredMetadataKeys,
        Set<String> supportedAgents,
        List<String> keywords
) {
}
