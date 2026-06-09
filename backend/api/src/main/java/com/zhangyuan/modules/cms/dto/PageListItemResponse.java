package com.zhangyuan.modules.cms.dto;

import java.time.Instant;
import java.util.List;

public record PageListItemResponse(
        Long id,
        String slug,
        String pageType,
        String defaultLocale,
        String status,
        Instant updatedAt,
        List<String> translations
) {
}
