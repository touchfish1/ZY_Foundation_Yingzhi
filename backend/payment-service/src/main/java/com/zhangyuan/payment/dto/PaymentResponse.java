package com.zhangyuan.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(String paymentNo, String orderNo, String channel, BigDecimal amount, String currency, String status, Instant createdAt, Instant paidAt) {}
