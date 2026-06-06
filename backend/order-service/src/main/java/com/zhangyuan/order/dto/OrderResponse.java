package com.zhangyuan.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
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
