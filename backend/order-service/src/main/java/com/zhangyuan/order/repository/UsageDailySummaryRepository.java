package com.zhangyuan.order.repository;

import com.zhangyuan.order.adapter.out.persistence.UsageDailySummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UsageDailySummaryRepository extends JpaRepository<UsageDailySummaryEntity, Long> {
    Optional<UsageDailySummaryEntity> findByUserIdAndDate(Long userId, LocalDate date);
    List<UsageDailySummaryEntity> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate start, LocalDate end);
}
