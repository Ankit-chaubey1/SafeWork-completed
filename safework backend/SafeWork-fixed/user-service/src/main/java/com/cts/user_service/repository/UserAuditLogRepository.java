package com.cts.user_service.repository;

import com.cts.user_service.entity.UserAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, Long> {
}
