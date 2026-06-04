package com.zhangyuan.modules.cms.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePageRequest(
        @NotBlank String slug,
        @NotBlank String title,
        String defaultLocale
) {
}
