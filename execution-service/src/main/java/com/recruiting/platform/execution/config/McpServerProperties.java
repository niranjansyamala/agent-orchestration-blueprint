package com.recruiting.platform.execution.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.mcp")
public class McpServerProperties {

    private String baseUrl = "http://localhost:8083";
    private String endpoint = "/mcp";
    private String audience = "recruiting-mcp-server";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
}
