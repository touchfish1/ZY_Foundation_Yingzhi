package com.zhangyuan.common.accesslog.domain.model;

import com.zhangyuan.common.dddframework.AggregateRoot;

import java.time.Instant;

public class AccessLog extends AggregateRoot<Long> {

    private String requestMethod;
    private String requestPath;
    private Integer responseStatus;
    private Long userId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private Long durationMs;
    private Instant createdAt;

    public AccessLog(String requestMethod, String requestPath, Integer responseStatus,
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
        this.createdAt = Instant.now();
    }

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
