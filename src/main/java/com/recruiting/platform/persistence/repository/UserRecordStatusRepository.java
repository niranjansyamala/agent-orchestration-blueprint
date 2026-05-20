package com.recruiting.platform.persistence.repository;

import com.recruiting.platform.persistence.entity.UserRecordStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRecordStatusRepository extends JpaRepository<UserRecordStatusEntity, String> {
}
