package com.recruiting.platform.execution.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AiPlatformProperties.class, McpServerProperties.class, ToolSelectionProperties.class})
public class ExecutionConfig {
}
