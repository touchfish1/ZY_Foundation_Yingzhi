package com.zhangyuan.user.dto;

public record UserQuotaResponse(Long userId, Long quotaUsed, Long quotaLimit, Integer rpmLimit) {
}
