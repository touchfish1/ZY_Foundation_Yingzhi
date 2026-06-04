package com.zhangyuan.user.dto;

import java.math.BigDecimal;

public record BalanceResponse(Long userId, BigDecimal balance) {}
