package com.recruiting.platform.persistence.repository;

import com.recruiting.platform.persistence.entity.HitlTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HitlTaskRepository extends JpaRepository<HitlTaskEntity, String> {
    Optional<HitlTaskEntity> findByWorkflowId(String workflowId);
}
