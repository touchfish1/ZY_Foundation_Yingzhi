package com.zhangyuan.auth.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MenuResponse(
    Long id,
    Long parentId,
    String name,
    String path,
    String icon,
    String menuType,
    Integer sortOrder,
    String status,
    List<String> permissionCodes,
    List<MenuResponse> children,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
