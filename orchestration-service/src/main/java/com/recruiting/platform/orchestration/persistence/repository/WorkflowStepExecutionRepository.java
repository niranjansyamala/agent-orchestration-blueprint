package com.recruiting.platform.orchestration.persistence.repository;

import com.recruiting.platform.orchestration.persistence.entity.WorkflowStepExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStepExecutionRepository extends JpaRepository<WorkflowStepExecutionEntity, String> {
}
