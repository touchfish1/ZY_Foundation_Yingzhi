package com.zhangyuan.auth.dto;

import java.util.List;

public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        List<String> roles
) {
}
