package com.zhangyuan.system.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(Long id, BigDecimal amount, BigDecimal balanceAfter, String transactionType, String description, Instant createdAt) {}
