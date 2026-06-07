package com.zhangyuan.common.operationlog.dto;

import java.time.Instant;

public record OperationLogResponse(
        Long id,
        Long operatorId,
        String operatorName,
        String operationType,
        String resourceType,
        String resourceId,
        String detail,
        String ipAddress,
        boolean success,
        String errorMessage,
        Instant createdAt
) {}
