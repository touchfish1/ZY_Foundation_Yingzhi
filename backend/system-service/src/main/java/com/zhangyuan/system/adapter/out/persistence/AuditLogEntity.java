package com.zhangyuan.system.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(nullable = false, length = 64)
    private String action;
    @Column(name = "resource_type", length = 64)
    private String resourceType;
    @Column(name = "resource_id", length = 128)
    private String resourceId;
    @Column(columnDefinition = "text")
    private String detail;
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public AuditLogEntity() {}

    public AuditLogEntity(Long userId, String action, String resourceType, String resourceId, String detail, String ipAddress, String userAgent) {
        this.userId = userId; this.action = action; this.resourceType = resourceType;
        this.resourceId = resourceId; this.detail = detail; this.ipAddress = ipAddress; this.userAgent = userAgent;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getDetail() { return detail; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public Instant getCreatedAt() { return createdAt; }
}
