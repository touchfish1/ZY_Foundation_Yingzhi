package com.zhangyuan.modules.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        String orderNo,
        Long planId,
        Long priceId,
        BigDecimal amount,
        String currency,
        String status,
        String snapshotJson,
        Instant createdAt,
        Instant paidAt
) {
}
