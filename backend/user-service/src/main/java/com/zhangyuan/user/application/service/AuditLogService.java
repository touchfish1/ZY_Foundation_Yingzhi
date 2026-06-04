package com.zhangyuan.user.application.service;

import com.zhangyuan.user.adapter.out.persistence.AuditLogEntity;
import com.zhangyuan.user.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    public AuditLogService(AuditLogRepository auditLogRepository) { this.auditLogRepository = auditLogRepository; }

    public void log(Long userId, String action, String resourceType, String resourceId, String detail, String ip, String ua) {
        auditLogRepository.save(new AuditLogEntity(userId, action, resourceType, resourceId, detail, ip, ua));
    }

    public void log(Long userId, String action, String detail) {
        log(userId, action, null, null, detail, null, null);
    }

    public Page<AuditLogEntity> getUserLogs(Long userId, int page, int pageSize) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, pageSize));
    }

    public Page<AuditLogEntity> getAllLogs(int page, int pageSize) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, pageSize));
    }
}
