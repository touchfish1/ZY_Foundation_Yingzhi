package com.zhangyuan.modules.order.domain.model;

import java.time.Instant;

public record PaymentInfo(String paymentNo, String channel, Instant paidAt) {
}
