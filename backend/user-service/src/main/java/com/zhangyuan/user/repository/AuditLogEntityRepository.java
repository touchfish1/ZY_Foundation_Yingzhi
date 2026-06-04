package com.zhangyuan.user.repository;

import com.zhangyuan.user.adapter.out.persistence.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogEntityRepository extends JpaRepository<AuditLogEntity, Long> {
    Page<AuditLogEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<AuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
