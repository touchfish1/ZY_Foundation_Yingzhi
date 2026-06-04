package com.zhangyuan.order.adapter.out.persistence;

import com.zhangyuan.order.domain.model.UserSubscription;
import com.zhangyuan.order.domain.repository.SubscriptionRepository;
import com.zhangyuan.order.repository.SubscriptionEntityRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaSubscriptionRepository implements SubscriptionRepository {
    private final SubscriptionEntityRepository repo;
    public JpaSubscriptionRepository(SubscriptionEntityRepository repo) { this.repo = repo; }

    @Override
    public Optional<UserSubscription> findByUserIdAndActive(Long userId) {
        return repo.findFirstByUserIdAndStatusOrderByExpiresAtDesc(userId, "active").map(this::toDomain);
    }

    @Override
    public List<UserSubscription> findByUserId(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public UserSubscription save(UserSubscription sub) {
        SubscriptionEntity entity;
        if (sub.getId() != null) {
            entity = repo.findById(sub.getId()).orElseThrow();
            entity.setStatus(sub.getStatus());
            entity.setExpiresAt(sub.getExpiresAt());
        } else {
            entity = new SubscriptionEntity();
            entity.setUserId(sub.getUserId());
            entity.setPlanCode(sub.getPlanCode());
            entity.setPlanName(sub.getPlanName());
            entity.setStatus(sub.getStatus());
            entity.setStartsAt(sub.getStartsAt());
            entity.setExpiresAt(sub.getExpiresAt());
        }
        SubscriptionEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    private UserSubscription toDomain(SubscriptionEntity e) {
        UserSubscription s = new UserSubscription();
        s.setId(e.getId()); s.setUserId(e.getUserId());
        s.setPlanCode(e.getPlanCode()); s.setPlanName(e.getPlanName());
        s.setStatus(e.getStatus()); s.setStartsAt(e.getStartsAt());
        s.setExpiresAt(e.getExpiresAt()); s.setCreatedAt(e.getCreatedAt()); s.setUpdatedAt(e.getUpdatedAt());
        return s;
    }
}
