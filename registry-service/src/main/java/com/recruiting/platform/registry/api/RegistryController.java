package com.recruiting.platform.registry.api;

import com.recruiting.platform.common.model.AgentResolutionRequest;
import com.recruiting.platform.common.model.ResolvedAgent;
import com.recruiting.platform.registry.service.AgentRegistryResolverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistryController {

    private final AgentRegistryResolverService resolverService;

    public RegistryController(AgentRegistryResolverService resolverService) {
        this.resolverService = resolverService;
    }

    @PostMapping("/internal/v1/agent-registry/resolve")
    @ResponseStatus(HttpStatus.OK)
    public ResolvedAgent resolve(@Valid @RequestBody AgentResolutionRequest request) {
        return resolverService.resolve(request)
                .orElseThrow(() -> new IllegalArgumentException("No active agent matched the routing request"));
    }
}
