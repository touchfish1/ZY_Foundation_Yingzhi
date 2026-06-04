package com.zhangyuan.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UsageRecordResponse(Long id, Long userId, String apiKey, String apiPath,
                                   Long tokensIn, Long tokensOut, BigDecimal cost,
                                   Integer durationMs, String status, Instant createdAt) {}
