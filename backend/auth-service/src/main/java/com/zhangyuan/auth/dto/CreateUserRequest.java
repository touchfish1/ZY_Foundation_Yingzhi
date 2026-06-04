package com.zhangyuan.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank String username,
    @NotBlank String password,
    String nickname,
    String email
) {}
