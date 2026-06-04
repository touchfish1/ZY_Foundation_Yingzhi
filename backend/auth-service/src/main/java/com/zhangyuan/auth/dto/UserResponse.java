package com.zhangyuan.auth.dto;

import java.time.Instant;

public record UserResponse(Long id, String username, String nickname, String email, String status, Instant createdAt) {}
