package com.recruiting.platform.orchestration.persistence.repository;

import com.recruiting.platform.orchestration.persistence.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, String> {
}
