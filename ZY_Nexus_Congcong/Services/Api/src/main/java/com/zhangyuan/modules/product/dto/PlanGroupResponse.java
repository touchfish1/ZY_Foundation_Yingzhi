package com.zhangyuan.modules.product.dto;

import java.util.List;

public record PlanGroupResponse(
        Long id,
        String code,
        String name,
        String description,
        String status,
        Integer sortOrder,
        List<PlanResponse> plans
) {
}
