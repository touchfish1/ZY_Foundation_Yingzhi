package com.zhangyuan.modules.payment.dto;

import java.time.Instant;

public record PaymentResponse(
        String paymentNo,
        String orderNo,
        String status,
        Instant paidAt
) {
}
