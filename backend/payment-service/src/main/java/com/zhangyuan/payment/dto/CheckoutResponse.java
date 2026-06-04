package com.zhangyuan.payment.dto;

public record CheckoutResponse(String paymentNo, String status, String mockPayUrl, String checkoutUrl) {}
