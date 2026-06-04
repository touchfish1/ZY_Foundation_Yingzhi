package com.zhangyuan.order.dto;

import java.math.BigDecimal;

public record UsageRecordRequest(Long userId, String apiKey, String apiPath, Long tokensIn, Long tokensOut,
                                  BigDecimal cost, Integer durationMs, String status) {}
