package com.recruiting.platform.orchestration.persistence.repository;

import com.recruiting.platform.orchestration.persistence.entity.WorkflowExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecutionEntity, String> {
    Optional<WorkflowExecutionEntity> findByRequestId(String requestId);
}
