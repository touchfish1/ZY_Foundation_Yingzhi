package com.zhangyuan.modules.product.dto;

import java.math.BigDecimal;

public record PriceResponse(
        Long id,
        String currency,
        String billingCycle,
        BigDecimal amount,
        BigDecimal originalAmount,
        String status
) {
}
