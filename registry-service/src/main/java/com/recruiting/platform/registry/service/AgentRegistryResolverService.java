package com.recruiting.platform.registry.service;

import com.recruiting.platform.common.domain.AgentStatus;
import com.recruiting.platform.common.domain.DispatchType;
import com.recruiting.platform.common.model.AgentResolutionRequest;
import com.recruiting.platform.common.model.ResolvedAgent;
import com.recruiting.platform.common.support.JsonSupport;
import com.recruiting.platform.registry.entity.AgentRegistryEntity;
import com.recruiting.platform.registry.repository.AgentRegistryRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AgentRegistryResolverService {

    private final AgentRegistryRepository repository;
    private final JsonSupport jsonSupport;

    public AgentRegistryResolverService(AgentRegistryRepository repository, JsonSupport jsonSupport) {
        this.repository = repository;
        this.jsonSupport = jsonSupport;
    }

    public Optional<ResolvedAgent> resolve(AgentResolutionRequest request) {
        return repository.findByStatus(AgentStatus.ACTIVE.name()).stream()
                .filter(entity -> supportsTenant(entity, request.tenantId()))
                .filter(entity -> supportsIntent(entity, request.intent()))
                .filter(entity -> supportsCapabilities(entity, request.requiredCapabilities()))
                .sorted(Comparator.comparing(AgentRegistryEntity::getPriority).reversed())
                .map(this::toResolvedAgent)
                .findFirst();
    }

    private boolean supportsTenant(AgentRegistryEntity entity, String tenantId) {
        List<String> tenants = jsonSupport.readStringList(entity.getTenantScope());
        return tenants.contains(tenantId) || tenants.contains("default");
    }

    private boolean supportsIntent(AgentRegistryEntity entity, String intent) {
        return jsonSupport.readStringList(entity.getSupportedIntents()).contains(intent);
    }

    private boolean supportsCapabilities(AgentRegistryEntity entity, List<String> requiredCapabilities) {
        return jsonSupport.readStringList(entity.getCapabilities()).containsAll(requiredCapabilities);
    }

    private ResolvedAgent toResolvedAgent(AgentRegistryEntity entity) {
        return new ResolvedAgent(
                entity.getAgentId(),
                entity.getVersion(),
                DispatchType.valueOf(entity.getDispatchType()),
                entity.getDispatchTarget(),
                entity.getPriority(),
                entity.getHealthStatus(),
                jsonSupport.readStringList(entity.getSupportedIntents()),
                jsonSupport.readStringList(entity.getCapabilities())
        );
    }
}
