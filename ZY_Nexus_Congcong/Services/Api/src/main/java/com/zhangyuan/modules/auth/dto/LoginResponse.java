package com.zhangyuan.modules.auth.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        AdminUserResponse user,
        List<String> permissions
) {
}
