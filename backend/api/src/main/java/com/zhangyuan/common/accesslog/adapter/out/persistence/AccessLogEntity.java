package com.zhangyuan.common.accesslog.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "access_log")
public class AccessLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_method", nullable = false, length = 16)
    private String requestMethod;

    @Column(name = "request_path", nullable = false, length = 512)
    private String requestPath;

    @Column(name = "response_status", nullable = false)
    private Integer responseStatus;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected AccessLogEntity() {
    }

    public AccessLogEntity(String requestMethod, String requestPath, Integer responseStatus,
                           Long userId, String username, String ipAddress, String userAgent,
                           Long durationMs) {
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.responseStatus = responseStatus;
        this.userId = userId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.durationMs = durationMs;
    }

    public Long getId() { return id; }
    public String getRequestMethod() { return requestMethod; }
    public String getRequestPath() { return requestPath; }
    public Integer getResponseStatus() { return responseStatus; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public Long getDurationMs() { return durationMs; }
    public Instant getCreatedAt() { return createdAt; }
}
