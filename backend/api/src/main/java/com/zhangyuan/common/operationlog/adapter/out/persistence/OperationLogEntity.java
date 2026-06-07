package com.zhangyuan.common.operationlog.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "operation_log")
public class OperationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    @Column(name = "operation_type", nullable = false, length = 32)
    private String operationType;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "resource_id", length = 128)
    private String resourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String detail;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(nullable = false, length = 16)
    private String result = "SUCCESS";

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected OperationLogEntity() {
    }

    public OperationLogEntity(Long operatorId, String operatorName, String operationType,
                              String resourceType, String resourceId, String detail,
                              String ipAddress) {
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.operationType = operationType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.detail = detail;
        this.ipAddress = ipAddress;
    }

    public Long getId() { return id; }
    public Long getOperatorId() { return operatorId; }
    public String getOperatorName() { return operatorName; }
    public String getOperationType() { return operationType; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getDetail() { return detail; }
    public String getIpAddress() { return ipAddress; }
    public String getResult() { return result; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }

    public void markFailed(String errorMessage) {
        this.result = "FAILURE";
        this.errorMessage = errorMessage;
    }
}
