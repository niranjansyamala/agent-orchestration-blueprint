package com.recruiting.platform.orchestration.client;

import com.recruiting.platform.common.model.AgentExecutionResult;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.orchestration.security.InternalJwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExecutionServiceClient {

    private final RestClient executionRestClient;
    private final InternalJwtTokenProvider tokenProvider;

    public ExecutionServiceClient(@Qualifier("executionRestClient") RestClient executionRestClient,
                                  InternalJwtTokenProvider tokenProvider) {
        this.executionRestClient = executionRestClient;
        this.tokenProvider = tokenProvider;
    }

    public AgentExecutionResult execute(ExecuteAgentRequest request) {
        return executionRestClient.post()
                .uri("/internal/v1/executions")
                .header("Authorization", "Bearer " + tokenProvider.tokenForAudience("execution-service"))
                .body(request)
                .retrieve()
                .body(AgentExecutionResult.class);
    }
}
