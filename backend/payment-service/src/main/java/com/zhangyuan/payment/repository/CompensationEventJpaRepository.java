package com.zhangyuan.payment.repository;

import com.zhangyuan.payment.adapter.out.persistence.CompensationEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompensationEventJpaRepository extends JpaRepository<CompensationEventEntity, Long> {
    List<CompensationEventEntity> findByEventStatusAndRetryCountLessThan(String status, int maxRetries, Pageable pageable);
}
