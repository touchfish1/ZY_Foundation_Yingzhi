package com.zhangyuan.common.accesslog.dto;

import java.time.Instant;

public record AccessLogResponse(
        Long id,
        String requestMethod,
        String requestPath,
        Integer responseStatus,
        Long userId,
        String username,
        String ipAddress,
        String userAgent,
        Long durationMs,
        Instant createdAt
) {}
