package com.moneyme.repository;

import com.moneyme.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActorUserIdOrderByCreatedAtDesc(Long actorUserId);
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
