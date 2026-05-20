package com.recruiting.platform.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruiting.platform.common.support.JsonSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonModuleConfig {

    @Bean
    public JsonSupport jsonSupport(ObjectMapper objectMapper) {
        return new JsonSupport(objectMapper);
    }
}
