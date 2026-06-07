package com.zhangyuan.common.accesslog.adapter.out.persistence;

import com.zhangyuan.common.accesslog.domain.model.AccessLog;
import com.zhangyuan.common.accesslog.domain.repository.AccessLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class JpaAccessLogRepository implements AccessLogRepository {

    private final AccessLogJpaRepository jpaRepository;

    public JpaAccessLogRepository(AccessLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AccessLog save(AccessLog log) {
        AccessLogEntity entity = toEntity(log);
        AccessLogEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AccessLog> query(String method, String path, Integer status, Long userId,
                                 Instant startTime, Instant endTime, int page, int pageSize) {
        Page<AccessLogEntity> pageResult = jpaRepository.query(
                method, path, status, userId, startTime, endTime, PageRequest.of(page - 1, pageSize));
        return pageResult.getContent().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long count(String method, String path, Integer status, Long userId,
                      Instant startTime, Instant endTime) {
        Page<AccessLogEntity> pageResult = jpaRepository.query(
                method, path, status, userId, startTime, endTime, PageRequest.of(0, 1));
        return pageResult.getTotalElements();
    }

    private AccessLogEntity toEntity(AccessLog log) {
        return new AccessLogEntity(
                log.getRequestMethod(),
                log.getRequestPath(),
                log.getResponseStatus(),
                log.getUserId(),
                log.getUsername(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getDurationMs()
        );
    }

    private AccessLog toDomain(AccessLogEntity entity) {
        AccessLog log = new AccessLog(
                entity.getRequestMethod(),
                entity.getRequestPath(),
                entity.getResponseStatus(),
                entity.getUserId(),
                entity.getUsername(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getDurationMs()
        );
        log.setId(entity.getId());
        return log;
    }
}
