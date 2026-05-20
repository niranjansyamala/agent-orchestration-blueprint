package com.recruiting.platform.orchestration.config;

import com.recruiting.platform.orchestration.security.InternalJwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({AiPlatformProperties.class, DownstreamServicesProperties.class, InternalJwtProperties.class})
public class OrchestrationConfig {

    @Bean
    @Qualifier("registryRestClient")
    public RestClient registryRestClient(DownstreamServicesProperties properties) {
        return RestClient.builder().baseUrl(properties.getRegistryBaseUrl()).build();
    }

    @Bean
    @Qualifier("executionRestClient")
    public RestClient executionRestClient(DownstreamServicesProperties properties) {
        return RestClient.builder().baseUrl(properties.getExecutionBaseUrl()).build();
    }
}
