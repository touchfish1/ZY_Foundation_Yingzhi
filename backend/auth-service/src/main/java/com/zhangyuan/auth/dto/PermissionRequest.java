package com.zhangyuan.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PermissionRequest(
    @NotBlank @Pattern(regexp = "^[a-z]+:[a-z:]+$") String code,
    @NotBlank String name,
    @NotBlank String module
) {}
