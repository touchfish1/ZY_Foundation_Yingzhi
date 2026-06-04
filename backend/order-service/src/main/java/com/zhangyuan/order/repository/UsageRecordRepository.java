package com.zhangyuan.order.repository;

import com.zhangyuan.order.adapter.out.persistence.UsageRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface UsageRecordRepository extends JpaRepository<UsageRecordEntity, Long> {
    Page<UsageRecordEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<UsageRecordEntity> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, Instant start, Instant end);
}
