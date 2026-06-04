package com.zhangyuan.modules.product.dto;

public record FeatureResponse(
        Long id,
        String featureName,
        String featureValue,
        Boolean included,
        Integer sortOrder
) {
}
