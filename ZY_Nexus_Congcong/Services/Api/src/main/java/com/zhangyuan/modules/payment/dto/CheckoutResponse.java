package com.zhangyuan.modules.payment.dto;

public record CheckoutResponse(
        String paymentNo,
        String status,
        String mockPayUrl,
        String checkoutUrl
) {
}
