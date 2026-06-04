package com.zhangyuan.modules.cms.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePageRequest(
    @NotBlank String slug,
    String defaultLocale
) {
}
