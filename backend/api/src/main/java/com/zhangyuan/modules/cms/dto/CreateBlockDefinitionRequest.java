package com.zhangyuan.modules.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateBlockDefinitionRequest(
        @NotBlank String type,
        @NotBlank String name,
        @NotNull Map<String, Object> schema,
        @NotNull Map<String, Object> defaultProps,
        @NotNull Integer sortOrder
) {
}
