package com.recruiting.platform.orchestration.client;

import com.recruiting.platform.common.model.AgentResolutionRequest;
import com.recruiting.platform.common.model.ResolvedAgent;
import com.recruiting.platform.orchestration.security.InternalJwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RegistryServiceClient {

    private final RestClient registryRestClient;
    private final InternalJwtTokenProvider tokenProvider;

    public RegistryServiceClient(@Qualifier("registryRestClient") RestClient registryRestClient,
                                 InternalJwtTokenProvider tokenProvider) {
        this.registryRestClient = registryRestClient;
        this.tokenProvider = tokenProvider;
    }

    public ResolvedAgent resolve(AgentResolutionRequest request) {
        return registryRestClient.post()
                .uri("/internal/v1/agent-registry/resolve")
                .header("Authorization", "Bearer " + tokenProvider.tokenForAudience("registry-service"))
                .body(request)
                .retrieve()
                .body(ResolvedAgent.class);
    }
}
