package com.zhangyuan.modules.cms.dto;

import java.time.Instant;

public record PageListItemResponse(
        Long id,
        String slug,
        String pageType,
        String defaultLocale,
        String status,
        Instant updatedAt
) {
}
