package com.recruiting.platform.registry.repository;

import com.recruiting.platform.registry.entity.AgentRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentRegistryRepository extends JpaRepository<AgentRegistryEntity, String> {
    List<AgentRegistryEntity> findByStatus(String status);
}
