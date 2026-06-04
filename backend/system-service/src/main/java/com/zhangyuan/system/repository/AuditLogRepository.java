package com.zhangyuan.system.repository;

import com.zhangyuan.system.adapter.out.persistence.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    Page<AuditLogEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<AuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
