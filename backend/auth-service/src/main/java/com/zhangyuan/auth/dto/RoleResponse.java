package com.zhangyuan.auth.dto;

import java.time.Instant;

public record RoleResponse(Long id, String code, String name, Instant createdAt) {}
