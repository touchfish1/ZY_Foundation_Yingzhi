package com.zhangyuan.common.operationlog.application;

import com.zhangyuan.common.operationlog.domain.model.OperationLog;
import com.zhangyuan.common.operationlog.domain.model.OperationType;
import com.zhangyuan.common.operationlog.domain.model.ResourceType;
import com.zhangyuan.common.operationlog.domain.repository.OperationLogRepository;
import com.zhangyuan.common.operationlog.dto.OperationLogResponse;
import com.zhangyuan.common.response.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogService.class);

    private final OperationLogRepository operationLogRepository;

    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    /**
     * 记录一条操作日志（内部由 AOP 切面调用，业务代码无需手动调用）。
     */
    @Transactional
    public OperationLog record(Long operatorId, String operatorName, OperationType operationType,
                               ResourceType resourceType, String resourceId, String detail,
                               String ipAddress) {
        OperationLog logEntry = new OperationLog(operatorId, operatorName, operationType,
                resourceType, resourceId, detail, ipAddress);
        OperationLog saved = operationLogRepository.save(logEntry);
        log.debug("Operation log recorded: type={}, resource={}, id={}, operator={}",
                operationType, resourceType, resourceId, operatorId);
        return saved;
    }

    /**
     * 更新日志结果为失败（由 AOP 切面在异常时调用）。
     */
    @Transactional
    public void markFailed(Long logId, String errorMessage) {
        operationLogRepository.findById(logId).ifPresent(logEntry -> {
            logEntry.markFailed(errorMessage);
            operationLogRepository.save(logEntry);
        });
    }

    /**
     * 查询操作日志（分页）。
     */
    @Transactional(readOnly = true)
    public PageResponse<OperationLogResponse> query(Long operatorId, ResourceType resourceType,
                                                     OperationType operationType,
                                                     Instant startTime, Instant endTime,
                                                     int page, int pageSize) {
        List<OperationLog> items = operationLogRepository.query(operatorId, resourceType,
                operationType, startTime, endTime, page, pageSize);
        long total = operationLogRepository.count(operatorId, resourceType, operationType,
                startTime, endTime);
        List<OperationLogResponse> responses = items.stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(responses, page, pageSize, total);
    }

    private OperationLogResponse toResponse(OperationLog logEntry) {
        return new OperationLogResponse(
                logEntry.getId(),
                logEntry.getOperatorId(),
                logEntry.getOperatorName(),
                logEntry.getOperationType().name(),
                logEntry.getResourceType().name(),
                logEntry.getResourceId(),
                logEntry.getDetail(),
                logEntry.getIpAddress(),
                logEntry.isSuccess(),
                logEntry.getErrorMessage(),
                logEntry.getCreatedAt()
        );
    }
}
