package com.zhangyuan.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePriceRequest(
        @NotNull Long planId,
        @NotBlank String currency,
        @NotBlank String billingCycle,
        @NotNull BigDecimal amount,
        BigDecimal originalAmount
) {
}
