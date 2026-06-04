package com.zhangyuan.user.adapter.out.persistence;

import com.zhangyuan.user.domain.model.AuditLog;
import com.zhangyuan.user.domain.repository.AuditLogRepository;
import com.zhangyuan.user.repository.AuditLogEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaAuditLogAdapter implements AuditLogRepository {

    private final AuditLogEntityRepository repo;

    public JpaAuditLogAdapter(AuditLogEntityRepository repo) {
        this.repo = repo;
    }

    @Override
    public void save(AuditLog log) {
        repo.save(toEntity(log));
    }

    @Override
    public Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) {
        Page<AuditLogEntity> entityPage = repo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<AuditLog> content = entityPage.getContent().stream()
                .map(this::toDomain)
                .toList();
        return new PageImpl<>(content, pageable, entityPage.getTotalElements());
    }

    @Override
    public Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable) {
        Page<AuditLogEntity> entityPage = repo.findAllByOrderByCreatedAtDesc(pageable);
        List<AuditLog> content = entityPage.getContent().stream()
                .map(this::toDomain)
                .toList();
        return new PageImpl<>(content, pageable, entityPage.getTotalElements());
    }

    private AuditLog toDomain(AuditLogEntity e) {
        AuditLog log = new AuditLog(
                e.getUserId(), e.getAction(), e.getResourceType(),
                e.getResourceId(), e.getDetail(), e.getIpAddress(), e.getUserAgent()
        );
        log.setId(e.getId());
        log.setCreatedAt(e.getCreatedAt());
        return log;
    }

    private AuditLogEntity toEntity(AuditLog log) {
        return new AuditLogEntity(
                log.getUserId(), log.getAction(), log.getResourceType(),
                log.getResourceId(), log.getDetail(), log.getIpAddress(), log.getUserAgent()
        );
    }
}
