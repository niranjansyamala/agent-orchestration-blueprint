package com.recruiting.platform.orchestration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.services")
public class DownstreamServicesProperties {

    private String registryBaseUrl = "http://localhost:8082";
    private String executionBaseUrl = "http://localhost:8081";

    public String getRegistryBaseUrl() {
        return registryBaseUrl;
    }

    public void setRegistryBaseUrl(String registryBaseUrl) {
        this.registryBaseUrl = registryBaseUrl;
    }

    public String getExecutionBaseUrl() {
        return executionBaseUrl;
    }

    public void setExecutionBaseUrl(String executionBaseUrl) {
        this.executionBaseUrl = executionBaseUrl;
    }
}
