package com.zhangyuan.order.application.service;

import com.zhangyuan.order.domain.model.Order;
import com.zhangyuan.order.domain.model.OrderNumber;
import com.zhangyuan.order.domain.model.OrderStatus;
import com.zhangyuan.order.domain.model.UserSubscription;
import com.zhangyuan.order.domain.repository.OrderRepository;
import com.zhangyuan.order.domain.repository.SubscriptionRepository;
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

    public FulfillmentService(OrderRepository orderRepository, SubscriptionRepository subscriptionRepository) {
        this.orderRepository = orderRepository;
        this.subscriptionRepository = subscriptionRepository;
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
        String snapshot = order.getSnapshotJson();
        String planCode = extractFromSnapshot(snapshot, "planCode");
        String planName = extractFromSnapshot(snapshot, "planName");
        int validityDays = 30; // Default 30 days
        try {
            validityDays = Integer.parseInt(extractFromSnapshot(snapshot, "validityDays"));
        } catch (Exception e) { /* use default */ }

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

        order.markFulfilled();
        orderRepository.save(order);
        log.info("Order {} fulfilled successfully", orderNo);
    }

    private String extractFromSnapshot(String snapshot, String key) {
        if (snapshot == null) return "";
        String search = "\"" + key + "\":\"";
        int start = snapshot.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        int end = snapshot.indexOf("\"", start);
        return end > start ? snapshot.substring(start, end) : "";
    }
}
