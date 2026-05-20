package com.recruiting.platform.registry;

import com.recruiting.platform.domain.AgentStatus;
import com.recruiting.platform.domain.DispatchType;
import com.recruiting.platform.domain.model.RoutingDecision;
import com.recruiting.platform.persistence.entity.AgentRegistryEntity;
import com.recruiting.platform.persistence.repository.AgentRegistryRepository;
import com.recruiting.platform.support.JsonSupport;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AgentRegistryResolver {

    private final AgentRegistryRepository repository;
    private final JsonSupport jsonSupport;

    public AgentRegistryResolver(AgentRegistryRepository repository, JsonSupport jsonSupport) {
        this.repository = repository;
        this.jsonSupport = jsonSupport;
    }

    public Optional<ResolvedAgent> resolve(String tenantId, RoutingDecision routingDecision) {
        return repository.findByStatus(AgentStatus.ACTIVE.name()).stream()
                .filter(agent -> supportsTenant(agent, tenantId))
                .filter(agent -> supportsIntent(agent, routingDecision))
                .filter(agent -> supportsCapabilities(agent, routingDecision.requiredCapabilities()))
                .sorted(Comparator.comparing(AgentRegistryEntity::getPriority).reversed())
                .map(this::toResolvedAgent)
                .findFirst();
    }

    private boolean supportsTenant(AgentRegistryEntity agent, String tenantId) {
        List<String> tenants = jsonSupport.readStringList(agent.getTenantScope());
        return tenants.contains(tenantId) || tenants.contains("default");
    }

    private boolean supportsIntent(AgentRegistryEntity agent, RoutingDecision routingDecision) {
        List<String> intents = jsonSupport.readStringList(agent.getSupportedIntents());
        return intents.contains(routingDecision.intent().name());
    }

    private boolean supportsCapabilities(AgentRegistryEntity agent, List<String> requiredCapabilities) {
        List<String> capabilities = jsonSupport.readStringList(agent.getCapabilities());
        return capabilities.containsAll(requiredCapabilities);
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
