package com.zhangyuan.order.repository;

import com.zhangyuan.order.adapter.out.persistence.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionEntityRepository extends JpaRepository<SubscriptionEntity, Long> {
    Optional<SubscriptionEntity> findFirstByUserIdAndStatusOrderByExpiresAtDesc(Long userId, String status);
    List<SubscriptionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
