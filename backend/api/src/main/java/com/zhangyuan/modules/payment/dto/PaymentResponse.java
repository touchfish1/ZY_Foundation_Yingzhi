package com.zhangyuan.modules.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String paymentNo,
        String orderNo,
        String status,
        Instant paidAt,
        String channel,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
}
