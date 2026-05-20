package com.recruiting.platform.persistence.repository;

import com.recruiting.platform.persistence.entity.WorkflowStepExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStepExecutionRepository extends JpaRepository<WorkflowStepExecutionEntity, String> {
}
