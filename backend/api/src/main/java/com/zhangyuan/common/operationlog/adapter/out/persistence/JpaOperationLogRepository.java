package com.zhangyuan.common.operationlog.adapter.out.persistence;

import com.zhangyuan.common.operationlog.domain.model.OperationLog;
import com.zhangyuan.common.operationlog.domain.model.OperationType;
import com.zhangyuan.common.operationlog.domain.model.ResourceType;
import com.zhangyuan.common.operationlog.domain.repository.OperationLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class JpaOperationLogRepository implements OperationLogRepository {

    private final OperationLogJpaRepository jpaRepository;

    public JpaOperationLogRepository(OperationLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OperationLog save(OperationLog log) {
        OperationLogEntity entity = toEntity(log);
        OperationLogEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OperationLog> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<OperationLog> query(Long operatorId, ResourceType resourceType, OperationType operationType,
                                    Instant startTime, Instant endTime, int page, int pageSize) {
        String resourceTypeStr = resourceType != null ? resourceType.name() : null;
        String operationTypeStr = operationType != null ? operationType.name() : null;
        Page<OperationLogEntity> pageResult = jpaRepository.query(
                operatorId, resourceTypeStr, operationTypeStr,
                startTime, endTime, PageRequest.of(page - 1, pageSize));
        return pageResult.getContent().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long count(Long operatorId, ResourceType resourceType, OperationType operationType,
                      Instant startTime, Instant endTime) {
        String resourceTypeStr = resourceType != null ? resourceType.name() : null;
        String operationTypeStr = operationType != null ? operationType.name() : null;
        Page<OperationLogEntity> pageResult = jpaRepository.query(
                operatorId, resourceTypeStr, operationTypeStr,
                startTime, endTime, PageRequest.of(0, 1));
        return pageResult.getTotalElements();
    }

    private OperationLogEntity toEntity(OperationLog log) {
        OperationLogEntity entity = new OperationLogEntity(
                log.getOperatorId(),
                log.getOperatorName(),
                log.getOperationType().name(),
                log.getResourceType().name(),
                log.getResourceId(),
                log.getDetail(),
                log.getIpAddress()
        );
        if (!log.isSuccess()) {
            entity.markFailed(log.getErrorMessage());
        }
        return entity;
    }

    private OperationLog toDomain(OperationLogEntity entity) {
        OperationLog log = new OperationLog(
                entity.getOperatorId(),
                entity.getOperatorName(),
                OperationType.valueOf(entity.getOperationType()),
                ResourceType.valueOf(entity.getResourceType()),
                entity.getResourceId(),
                entity.getDetail(),
                entity.getIpAddress()
        );
        log.setId(entity.getId());
        if ("FAILURE".equals(entity.getResult())) {
            log.markFailed(entity.getErrorMessage());
        }
        return log;
    }
}
