package com.zhangyuan.user.dto;

public record UserResponse(Long id, String email, String nickname, String apiKey, Long quotaUsed, Long quotaLimit, String status) {}
