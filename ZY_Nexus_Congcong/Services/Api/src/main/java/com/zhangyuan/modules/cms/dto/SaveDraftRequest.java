package com.zhangyuan.modules.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SaveDraftRequest(
        @NotBlank String title,
        String seoTitle,
        String seoDescription,
        String seoKeywords,
        @NotNull Map<String, Object> content,
        String remark
) {
}
