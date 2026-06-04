package com.zhangyuan.modules.product.dto;

import java.util.List;

public record PlanResponse(
        Long id,
        Long groupId,
        String code,
        String name,
        String description,
        String badge,
        String status,
        Integer sortOrder,
        List<PriceResponse> prices,
        List<FeatureResponse> features
) {
}
