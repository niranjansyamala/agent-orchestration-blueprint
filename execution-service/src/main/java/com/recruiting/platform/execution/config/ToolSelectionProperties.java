package com.recruiting.platform.execution.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.tool-selection")
public class ToolSelectionProperties {

    private int maxVisibleTools = 5;

    public int getMaxVisibleTools() {
        return maxVisibleTools;
    }

    public void setMaxVisibleTools(int maxVisibleTools) {
        this.maxVisibleTools = maxVisibleTools;
    }
}
