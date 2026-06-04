package com.zhangyuan.user.domain.model;

import java.time.Instant;

public class AuditLog {
    private Long id;
    private Long userId;
    private String action;
    private String resourceType;
    private String resourceId;
    private String detail;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;

    public AuditLog() {}

    public AuditLog(Long userId, String action, String resourceType, String resourceId, String detail, String ipAddress, String userAgent) {
        this.userId = userId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.detail = detail;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
