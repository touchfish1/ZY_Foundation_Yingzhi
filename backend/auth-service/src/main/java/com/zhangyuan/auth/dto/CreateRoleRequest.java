package com.zhangyuan.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(@NotBlank String code, @NotBlank String name) {}
