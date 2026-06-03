package com.zhangyuan.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFeatureRequest(
        @NotNull Long planId,
        @NotBlank String featureName,
        String featureValue,
        Boolean included,
        Integer sortOrder
) {
}
