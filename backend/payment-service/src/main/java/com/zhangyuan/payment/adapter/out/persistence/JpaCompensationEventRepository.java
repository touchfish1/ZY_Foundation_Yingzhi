package com.zhangyuan.payment.adapter.out.persistence;

import com.zhangyuan.payment.domain.model.CompensationEvent;
import com.zhangyuan.payment.domain.repository.CompensationEventRepository;
import com.zhangyuan.payment.repository.CompensationEventJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaCompensationEventRepository implements CompensationEventRepository {

    private final CompensationEventJpaRepository repo;

    public JpaCompensationEventRepository(CompensationEventJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public CompensationEvent save(CompensationEvent event) {
        CompensationEventEntity entity = toEntity(event);
        CompensationEventEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<CompensationEvent> findPendingEvents(int maxRetries, int limit) {
        return repo.findByEventStatusAndRetryCountLessThan("PENDING", maxRetries, PageRequest.of(0, limit))
                .stream().map(this::toDomain).toList();
    }

    private CompensationEvent toDomain(CompensationEventEntity e) {
        CompensationEvent d = new CompensationEvent(e.getPaymentNo(), e.getEventType(), e.getPayloadJson());
        d.setId(e.getId());
        d.setEventStatus(e.getEventStatus());
        d.setRetryCount(e.getRetryCount());
        d.setMaxRetries(e.getMaxRetries());
        d.setLastError(e.getLastError());
        d.setCreatedAt(e.getCreatedAt());
        d.setLastRetryAt(e.getLastRetryAt());
        return d;
    }

    private CompensationEventEntity toEntity(CompensationEvent d) {
        CompensationEventEntity e = new CompensationEventEntity(d.getPaymentNo(), d.getEventType(), d.getPayloadJson());
        e.setId(d.getId());
        e.setEventStatus(d.getEventStatus());
        e.setRetryCount(d.getRetryCount());
        e.setMaxRetries(d.getMaxRetries());
        e.setLastError(d.getLastError());
        e.setCreatedAt(d.getCreatedAt());
        e.setLastRetryAt(d.getLastRetryAt());
        return e;
    }
}
