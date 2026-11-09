package com.moneyme.service;

import com.moneyme.entity.AuditLog;
import com.moneyme.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorUserId, String action, String entityType, String entityId, String metadata) {
        AuditLog auditLog = new AuditLog(actorUserId, action, entityType, entityId, null, metadata);
        auditLogRepository.save(auditLog);
    }
}
