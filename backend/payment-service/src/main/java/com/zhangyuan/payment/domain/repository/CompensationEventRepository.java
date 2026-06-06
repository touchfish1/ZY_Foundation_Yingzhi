package com.zhangyuan.payment.domain.repository;

import com.zhangyuan.payment.domain.model.CompensationEvent;

import java.util.List;

public interface CompensationEventRepository {
    CompensationEvent save(CompensationEvent event);
    List<CompensationEvent> findPendingEvents(int maxRetries, int limit);
}
