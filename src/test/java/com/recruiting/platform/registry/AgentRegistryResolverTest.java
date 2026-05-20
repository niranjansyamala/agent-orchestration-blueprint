package com.recruiting.platform.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruiting.platform.domain.model.RoutingDecision;
import com.recruiting.platform.persistence.entity.AgentRegistryEntity;
import com.recruiting.platform.persistence.repository.AgentRegistryRepository;
import com.recruiting.platform.support.JsonSupport;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.recruiting.platform.domain.IntentType.SCHEDULE_INTERVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentRegistryResolverTest {

    @Test
    void resolvesHighestPriorityMatchingAgent() {
        AgentRegistryRepository repository = mock(AgentRegistryRepository.class);
        JsonSupport jsonSupport = new JsonSupport(new ObjectMapper());
        AgentRegistryResolver resolver = new AgentRegistryResolver(repository, jsonSupport);

        AgentRegistryEntity entity = new AgentRegistryEntity();
        entity.setAgentId("interview_scheduling_agent");
        entity.setAgentName("Interview Scheduling Agent");
        entity.setVersion("v1");
        entity.setStatus("ACTIVE");
        entity.setSupportedIntents("[\"SCHEDULE_INTERVIEW\"]");
        entity.setCapabilities("[\"calendar_lookup\",\"status_update\"]");
        entity.setDispatchType("WORKFLOW");
        entity.setDispatchTarget("interview_scheduling_agent");
        entity.setTenantScope("[\"default\"]");
        entity.setPriority(95);
        entity.setHealthStatus("healthy");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        when(repository.findByStatus("ACTIVE")).thenReturn(List.of(entity));

        Optional<ResolvedAgent> resolvedAgent = resolver.resolve(
                "default",
                new RoutingDecision(SCHEDULE_INTERVIEW, "interview_scheduling_agent", 0.8,
                        List.of("calendar_lookup", "status_update"), false)
        );

        assertTrue(resolvedAgent.isPresent());
        assertEquals("interview_scheduling_agent", resolvedAgent.get().agentId());
    }
}
