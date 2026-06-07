package com.zhangyuan.common.operationlog.domain.repository;

import com.zhangyuan.common.operationlog.domain.model.OperationLog;
import com.zhangyuan.common.operationlog.domain.model.OperationType;
import com.zhangyuan.common.operationlog.domain.model.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OperationLogRepository {

    OperationLog save(OperationLog log);

    Optional<OperationLog> findById(Long id);

    List<OperationLog> query(Long operatorId, ResourceType resourceType, OperationType operationType,
                             Instant startTime, Instant endTime, int page, int pageSize);

    long count(Long operatorId, ResourceType resourceType, OperationType operationType,
               Instant startTime, Instant endTime);
}
