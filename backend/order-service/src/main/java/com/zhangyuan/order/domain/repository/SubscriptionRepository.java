package com.zhangyuan.order.domain.repository;

import com.zhangyuan.order.domain.model.UserSubscription;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {
    Optional<UserSubscription> findByUserIdAndActive(Long userId);
    List<UserSubscription> findByUserId(Long userId);
    UserSubscription save(UserSubscription subscription);
}
