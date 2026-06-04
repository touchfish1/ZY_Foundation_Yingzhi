package com.zhangyuan.system.dto;

public record UserResponse(Long id, String email, String nickname, String apiKey, Long quotaUsed, Long quotaLimit, String status) {}
