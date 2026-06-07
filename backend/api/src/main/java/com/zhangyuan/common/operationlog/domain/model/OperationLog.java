package com.zhangyuan.common.operationlog.domain.model;

import com.zhangyuan.common.dddframework.AggregateRoot;

import java.time.Instant;

public class OperationLog extends AggregateRoot<Long> {

    private Long operatorId;
    private String operatorName;
    private OperationType operationType;
    private ResourceType resourceType;
    private String resourceId;
    private String detail;
    private String ipAddress;
    private boolean success;
    private String errorMessage;
    private Instant createdAt;

    public OperationLog(Long operatorId, String operatorName, OperationType operationType,
                        ResourceType resourceType, String resourceId, String detail,
                        String ipAddress) {
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.operationType = operationType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.detail = detail;
        this.ipAddress = ipAddress;
        this.success = true;
        this.createdAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }

    public Long getOperatorId() { return operatorId; }
    public String getOperatorName() { return operatorName; }
    public OperationType getOperationType() { return operationType; }
    public ResourceType getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getDetail() { return detail; }
    public String getIpAddress() { return ipAddress; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
}
