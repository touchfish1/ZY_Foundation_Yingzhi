package com.zhangyuan.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(@NotBlank String orderNo, @NotBlank String channel) {}
