package com.zhangyuan.modules.product.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlanGroupRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        Integer sortOrder
) {
}
