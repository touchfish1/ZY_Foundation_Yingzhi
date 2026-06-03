package com.zhangyuan.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePlanRequest(
        @NotNull Long groupId,
        @NotBlank String code,
        @NotBlank String name,
        String description,
        String badge,
        Integer sortOrder
) {
}
