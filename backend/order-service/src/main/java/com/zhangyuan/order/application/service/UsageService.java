package com.zhangyuan.order.application.service;

import com.zhangyuan.order.adapter.out.persistence.UsageDailySummaryEntity;
import com.zhangyuan.order.adapter.out.persistence.UsageRecordEntity;
import com.zhangyuan.order.dto.UsageRecordResponse;
import com.zhangyuan.order.dto.UsageSummaryResponse;
import com.zhangyuan.order.repository.UsageDailySummaryRepository;
import com.zhangyuan.order.repository.UsageRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class UsageService {
    private final UsageRecordRepository usageRecordRepository;
    private final UsageDailySummaryRepository dailySummaryRepository;

    public UsageService(UsageRecordRepository usageRecordRepository,
                        UsageDailySummaryRepository dailySummaryRepository) {
        this.usageRecordRepository = usageRecordRepository;
        this.dailySummaryRepository = dailySummaryRepository;
    }

    @Transactional
    public void recordUsage(Long userId, String apiKey, String apiPath, Long tokensIn, Long tokensOut,
                            BigDecimal cost, Integer durationMs, String status) {
        usageRecordRepository.save(new UsageRecordEntity(userId, apiKey, apiPath, tokensIn, tokensOut, cost, durationMs, status));

        // Update daily summary
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        UsageDailySummaryEntity summary = dailySummaryRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> new UsageDailySummaryEntity(userId, today));
        summary.setTotalCalls(summary.getTotalCalls() + 1);
        summary.setTotalTokensIn(summary.getTotalTokensIn() + (tokensIn != null ? tokensIn : 0));
        summary.setTotalTokensOut(summary.getTotalTokensOut() + (tokensOut != null ? tokensOut : 0));
        summary.setTotalCost(summary.getTotalCost().add(cost != null ? cost : BigDecimal.ZERO));
        dailySummaryRepository.save(summary);
    }

    @Transactional(readOnly = true)
    public Page<UsageRecordResponse> getUserUsage(Long userId, int page, int pageSize) {
        return usageRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, pageSize))
                .map(e -> new UsageRecordResponse(e.getId(), e.getUserId(), e.getApiKey(), e.getApiPath(),
                        e.getTokensIn(), e.getTokensOut(), e.getCost(), e.getDurationMs(), e.getStatus(), e.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public UsageSummaryResponse getDailySummary(Long userId, LocalDate start, LocalDate end) {
        var summaries = dailySummaryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end);
        int totalCalls = summaries.stream().mapToInt(UsageDailySummaryEntity::getTotalCalls).sum();
        long totalTokensIn = summaries.stream().mapToLong(UsageDailySummaryEntity::getTotalTokensIn).sum();
        long totalTokensOut = summaries.stream().mapToLong(UsageDailySummaryEntity::getTotalTokensOut).sum();
        BigDecimal totalCost = summaries.stream().map(UsageDailySummaryEntity::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new UsageSummaryResponse(userId, totalCalls, totalTokensIn, totalTokensOut, totalCost,
                summaries.stream().map(s -> new UsageSummaryResponse.DailySummary(s.getDate(), s.getTotalCalls(),
                        s.getTotalTokensIn(), s.getTotalTokensOut(), s.getTotalCost())).toList());
    }
}
