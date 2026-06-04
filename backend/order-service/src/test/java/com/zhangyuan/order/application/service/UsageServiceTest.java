package com.zhangyuan.order.application.service;

import com.zhangyuan.order.adapter.out.persistence.UsageDailySummaryEntity;
import com.zhangyuan.order.adapter.out.persistence.UsageRecordEntity;
import com.zhangyuan.order.repository.UsageDailySummaryRepository;
import com.zhangyuan.order.repository.UsageRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsageServiceTest {

    private final UsageRecordRepository usageRecordRepo = mock(UsageRecordRepository.class);
    private final UsageDailySummaryRepository dailySummaryRepo = mock(UsageDailySummaryRepository.class);
    private UsageService service;

    @BeforeEach
    void setUp() { service = new UsageService(usageRecordRepo, dailySummaryRepo); }

    @Test
    void recordUsage_createsRecordAndUpdatesSummary() {
        when(dailySummaryRepo.findByUserIdAndDate(any(), any())).thenReturn(Optional.empty());
        when(dailySummaryRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.recordUsage(1L, "sk-test", "/v1/chat", 100L, 50L,
                BigDecimal.valueOf(0.01), 200, "success");

        verify(usageRecordRepo).save(any());
        verify(dailySummaryRepo).save(argThat(s ->
            s.getTotalCalls() == 1 &&
            s.getTotalTokensIn() == 100L &&
            s.getTotalTokensOut() == 50L
        ));
    }

    @Test
    void recordUsage_multipleCalls_accumulatesSummary() {
        when(dailySummaryRepo.findByUserIdAndDate(any(), any()))
            .thenReturn(Optional.of(new UsageDailySummaryEntity(1L, LocalDate.now())))
            .thenReturn(Optional.of(new UsageDailySummaryEntity(1L, LocalDate.now())));

        service.recordUsage(1L, "sk-1", "/v1/chat", 100L, 50L, BigDecimal.valueOf(0.01), 200, "success");
        service.recordUsage(1L, "sk-2", "/v1/chat", 200L, 100L, BigDecimal.valueOf(0.02), 300, "success");

        verify(dailySummaryRepo, times(2)).save(any());
    }

    @Test
    void getDailySummary_returnsAggregatedData() {
        var today = LocalDate.now();
        var s1 = new UsageDailySummaryEntity(1L, today);
        s1.setTotalCalls(10); s1.setTotalTokensIn(1000L); s1.setTotalTokensOut(500L); s1.setTotalCost(BigDecimal.valueOf(0.5));
        var s2 = new UsageDailySummaryEntity(1L, today.minusDays(1));
        s2.setTotalCalls(5); s2.setTotalTokensIn(500L); s2.setTotalTokensOut(200L); s2.setTotalCost(BigDecimal.valueOf(0.25));

        when(dailySummaryRepo.findByUserIdAndDateBetweenOrderByDateAsc(1L, today.minusDays(7), today))
            .thenReturn(List.of(s2, s1));

        var summary = service.getDailySummary(1L, today.minusDays(7), today);

        assertThat(summary.totalCalls()).isEqualTo(15);
        assertThat(summary.totalTokensIn()).isEqualTo(1500L);
        assertThat(summary.totalTokensOut()).isEqualTo(700L);
        assertThat(summary.totalCost()).isEqualByComparingTo("0.75");
        assertThat(summary.dailySummaries()).hasSize(2);
    }
}
