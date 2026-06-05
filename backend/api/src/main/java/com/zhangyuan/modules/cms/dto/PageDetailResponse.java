package com.zhangyuan.modules.cms.dto;

import java.util.List;

public record PageDetailResponse(
        Long id,
        String slug,
        String pageType,
        String defaultLocale,
        String status,
        List<PageTranslationResponse> translations
) {
}
