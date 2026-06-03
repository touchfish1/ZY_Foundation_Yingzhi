package com.zhangyuan.modules.auth.domain.model;

import java.util.List;

public record LoginResult(String accessToken, long expiresIn, User user, List<String> permissions) {
}
