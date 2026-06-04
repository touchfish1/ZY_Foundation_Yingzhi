package com.zhangyuan.modules.cms.dto;

public record PageTranslationResponse(
        String locale,
        String title,
        String seoTitle,
        String seoDescription,
        String seoKeywords,
        Long draftVersionId,
        Long publishedVersionId,
        String status
) {
}
