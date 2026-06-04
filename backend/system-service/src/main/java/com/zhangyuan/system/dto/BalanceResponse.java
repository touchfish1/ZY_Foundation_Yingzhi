package com.zhangyuan.system.dto;

import java.math.BigDecimal;

public record BalanceResponse(Long userId, BigDecimal balance) {}
