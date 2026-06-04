package com.zhangyuan.auth.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record MenuRequest(
    Long parentId,
    @NotBlank String name,
    String path,
    String icon,
    @NotBlank String menuType,
    Integer sortOrder,
    String status,
    List<String> permissionCodes
) {}
