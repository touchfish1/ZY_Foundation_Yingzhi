package com.zhangyuan.order.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank String planCode,
        @NotBlank String billingCycle,
        @NotBlank String currency
) {
}
