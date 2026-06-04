package com.zhangyuan.user.domain.repository;

import com.zhangyuan.user.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogRepository {
    void save(AuditLog log);
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
