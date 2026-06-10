package com.zhangyuan.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSnapshot(
        String planCode,
        String billingCycle,
        String currency,
        Instant createdAt,
        String planName,
        Integer validityDays,
        BigDecimal amount
) {
}
