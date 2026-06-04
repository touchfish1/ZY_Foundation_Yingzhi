package com.zhangyuan.system.dto;

import java.time.Instant;

public record AuditLogResponse(Long id, Long userId, String action, String resourceType,
                                String resourceId, String detail, String ipAddress, Instant createdAt) {}
