package com.zhangyuan.order.application.service;

import com.zhangyuan.order.adapter.out.persistence.SubscriptionEntity;
import com.zhangyuan.order.client.UserServiceClient;
import com.zhangyuan.order.repository.SubscriptionEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class SubscriptionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduler.class);

    /**
     * Default quota assigned to users when their subscription expires.
     * Matches the trial plan quota in FulfillmentService.resolveQuotaLimit().
     */
    private static final long DEFAULT_QUOTA = 10_000L;

    private final SubscriptionEntityRepository subscriptionEntityRepository;
    private final UserServiceClient userServiceClient;

    public SubscriptionScheduler(SubscriptionEntityRepository subscriptionEntityRepository,
                                  UserServiceClient userServiceClient) {
        this.subscriptionEntityRepository = subscriptionEntityRepository;
        this.userServiceClient = userServiceClient;
    }

    /**
     * Daily at 2 AM: find all active subscriptions that have passed their expires_at,
     * mark them as expired, and reset the user's quota to the default (trial) level.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkExpiredSubscriptions() {
        Instant now = Instant.now();
        List<SubscriptionEntity> expiredSubscriptions =
                subscriptionEntityRepository.findByStatusAndExpiresAtBefore("active", now);

        int processed = 0;
        for (SubscriptionEntity sub : expiredSubscriptions) {
            sub.setStatus("expired");
            subscriptionEntityRepository.save(sub);

            try {
                userServiceClient.updateQuota(sub.getUserId(), DEFAULT_QUOTA);
                log.info("Subscription {} for userId={} expired, quota reset to {}",
                        sub.getId(), sub.getUserId(), DEFAULT_QUOTA);
            } catch (Exception e) {
                log.error("Failed to reset quota for userId={} after subscription {} expired",
                        sub.getUserId(), sub.getId(), e);
            }
            processed++;
        }

        if (processed > 0) {
            log.info("checkExpiredSubscriptions completed: {} subscriptions expired", processed);
        }
    }

    /**
     * Hourly: find active subscriptions expiring in the next 7 days
     * and log notification events for future integration with email/webhook systems.
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void notifyExpiringSoon() {
        Instant now = Instant.now();
        Instant sevenDaysLater = now.plus(7, ChronoUnit.DAYS);
        List<SubscriptionEntity> expiringSoon =
                subscriptionEntityRepository.findByStatusAndExpiresAtBetween("active", now, sevenDaysLater);

        for (SubscriptionEntity sub : expiringSoon) {
            log.info("Notification event: subscription {} for userId={} (plan={}) expires at {}",
                    sub.getId(), sub.getUserId(), sub.getPlanCode(), sub.getExpiresAt());
        }

        if (!expiringSoon.isEmpty()) {
            log.info("notifyExpiringSoon completed: {} subscriptions expiring within 7 days",
                    expiringSoon.size());
        }
    }
}
