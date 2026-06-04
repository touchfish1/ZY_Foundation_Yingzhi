package com.zhangyuan.modules.auth.dto;

import java.time.Instant;

public record RoleResponse(Long id, String code, String name, Instant createdAt) {}
