package com.recruiting.platform.registry.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruiting.platform.common.model.AgentResolutionRequest;
import com.recruiting.platform.common.support.JsonSupport;
import com.recruiting.platform.registry.entity.AgentRegistryEntity;
import com.recruiting.platform.registry.repository.AgentRegistryRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentRegistryResolverServiceTest {

    @Test
    void resolvesMatchingAgent() {
        AgentRegistryRepository repository = mock(AgentRegistryRepository.class);
        JsonSupport jsonSupport = new JsonSupport(new ObjectMapper());
        AgentRegistryResolverService resolverService = new AgentRegistryResolverService(repository, jsonSupport);

        AgentRegistryEntity entity = new AgentRegistryEntity();
        entity.setAgentId("candidate_status_agent");
        entity.setAgentName("Candidate Status Agent");
        entity.setVersion("v1");
        entity.setStatus("ACTIVE");
        entity.setSupportedIntents("[\"UPDATE_CANDIDATE_STATUS\"]");
        entity.setCapabilities("[\"db_read\",\"db_update\"]");
        entity.setDispatchType("WORKFLOW");
        entity.setDispatchTarget("candidate_status_agent");
        entity.setTenantScope("[\"default\"]");
        entity.setPriority(90);
        entity.setHealthStatus("healthy");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        when(repository.findByStatus("ACTIVE")).thenReturn(List.of(entity));

        var result = resolverService.resolve(new AgentResolutionRequest(
                "default",
                "UPDATE_CANDIDATE_STATUS",
                List.of("db_read", "db_update")
        ));

        assertTrue(result.isPresent());
        assertEquals("candidate_status_agent", result.get().agentId());
    }
}
