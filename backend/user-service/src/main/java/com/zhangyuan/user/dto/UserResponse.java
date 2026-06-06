package com.zhangyuan.user.dto;

public record UserResponse(Long id, String email, String nickname, String apiKey, Long quotaUsed, Long quotaLimit, String status) {

    public static UserResponse masked(UserResponse resp) {
        if (resp.apiKey == null || resp.apiKey.length() <= 8) return resp;
        return new UserResponse(resp.id, resp.email, resp.nickname,
                resp.apiKey.substring(0, 4) + "****" + resp.apiKey.substring(resp.apiKey.length() - 4),
                resp.quotaUsed, resp.quotaLimit, resp.status);
    }
}
