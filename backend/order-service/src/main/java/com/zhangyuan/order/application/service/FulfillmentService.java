package com.zhangyuan.order.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.order.client.UserServiceClient;
import com.zhangyuan.order.domain.model.Order;
import com.zhangyuan.order.domain.model.OrderNumber;
import com.zhangyuan.order.domain.model.OrderStatus;
import com.zhangyuan.order.domain.model.UserSubscription;
import com.zhangyuan.order.domain.repository.OrderRepository;
import com.zhangyuan.order.domain.repository.SubscriptionRepository;
import com.zhangyuan.order.dto.OrderSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FulfillmentService {
    private static final Logger log = LoggerFactory.getLogger(FulfillmentService.class);
    private final OrderRepository orderRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper;

    public FulfillmentService(OrderRepository orderRepository, SubscriptionRepository subscriptionRepository,
                              UserServiceClient userServiceClient, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userServiceClient = userServiceClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void fulfillOrder(String orderNo) {
        Order order = orderRepository.findByOrderNo(new OrderNumber(orderNo))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));

        if (order.getStatus() == OrderStatus.FULFILLED) {
            log.warn("Idempotent call: order {} already FULFILLED, skipped", orderNo);
            return;
        }

        if (order.getStatus() != OrderStatus.PAID) {
            log.warn("Order {} is not PAID (status: {}), cannot fulfill", orderNo, order.getStatus());
            return;
        }

        // Parse snapshot to get plan info
        OrderSnapshot snapshot;
        try {
            snapshot = objectMapper.readValue(order.getSnapshotJson(), OrderSnapshot.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse snapshot JSON for order: " + orderNo, e);
        }

        String planCode = snapshot.planCode();
        String planName = snapshot.planName();
        int validityDays = snapshot.validityDays() != null ? snapshot.validityDays() : 30;

        // Assign or extend subscription
        Optional<UserSubscription> existing = subscriptionRepository.findByUserIdAndActive(order.getUserId());
        UserSubscription sub;
        if (existing.isPresent()) {
            sub = existing.get();
            sub.extend(validityDays);
            log.info("Extending subscription {} for user {} by {} days", sub.getId(), order.getUserId(), validityDays);
        } else {
            sub = new UserSubscription(order.getUserId(), planCode, planName, validityDays);
            log.info("Creating new subscription for user {}: {} ({} days)", order.getUserId(), planName, validityDays);
        }
        subscriptionRepository.save(sub);

        // Provision quota in user-service based on plan
        long quotaLimit = resolveQuotaLimit(planCode);
        try {
            userServiceClient.updateQuota(order.getUserId(), quotaLimit);
            log.info("Quota updated for userId={}: limit={}", order.getUserId(), quotaLimit);
        } catch (Exception e) {
            log.error("Failed to update quota for userId={}, fulfillment continues", order.getUserId(), e);
        }

        order.markFulfilled();
        orderRepository.save(order);
        log.info("Order {} fulfilled successfully", orderNo);
    }

    private long resolveQuotaLimit(String planCode) {
        if (planCode == null) return 100_000L;
        return switch (planCode.toLowerCase()) {
            case "basic" -> 100_000L;
            case "pro" -> 1_000_000L;
            case "enterprise" -> 10_000_000L;
            case "trial" -> 10_000L;
            default -> 100_000L;
        };
    }
}
