package com.zhangyuan.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UsageSummaryResponse(Long userId, int totalCalls, long totalTokensIn, long totalTokensOut,
                                    BigDecimal totalCost, List<DailySummary> dailySummaries) {
    public record DailySummary(LocalDate date, int calls, long tokensIn, long tokensOut, BigDecimal cost) {}
}
