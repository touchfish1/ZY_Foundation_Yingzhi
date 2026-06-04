package com.zhangyuan.order.application.service;

import com.zhangyuan.order.domain.model.UserSubscription;
import com.zhangyuan.order.domain.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionQueryService {
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionQueryService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<UserSubscription> findActiveByUser(Long userId) {
        return subscriptionRepository.findByUserIdAndActive(userId);
    }

    @Transactional(readOnly = true)
    public List<UserSubscription> findByUser(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }
}
