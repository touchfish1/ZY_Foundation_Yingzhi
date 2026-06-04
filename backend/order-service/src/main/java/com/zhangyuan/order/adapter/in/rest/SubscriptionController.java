package com.zhangyuan.order.adapter.in.rest;

import com.zhangyuan.order.application.service.SubscriptionQueryService;
import com.zhangyuan.order.common.ApiResponse;
import com.zhangyuan.order.domain.model.UserSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);
    private final SubscriptionQueryService subscriptionQueryService;

    public SubscriptionController(SubscriptionQueryService subscriptionQueryService) {
        this.subscriptionQueryService = subscriptionQueryService;
    }

    @GetMapping("/active")
    public ApiResponse<SubscriptionDTO> getActiveSubscription(@RequestParam Long userId) {
        log.info("Getting active subscription for user: {}", userId);
        return subscriptionQueryService.findActiveByUser(userId)
                .map(sub -> ApiResponse.ok(toDTO(sub)))
                .orElse(ApiResponse.ok(null));
    }

    @GetMapping
    public ApiResponse<List<SubscriptionDTO>> listSubscriptions(@RequestParam Long userId) {
        log.info("Listing subscriptions for user: {}", userId);
        var list = subscriptionQueryService.findByUser(userId).stream()
                .map(this::toDTO).toList();
        return ApiResponse.ok(list);
    }

    private SubscriptionDTO toDTO(UserSubscription sub) {
        return new SubscriptionDTO(sub.getId(), sub.getUserId(), sub.getPlanCode(),
                sub.getPlanName(), sub.getStatus(), sub.getStartsAt(), sub.getExpiresAt(),
                sub.isActive());
    }
}

record SubscriptionDTO(Long id, Long userId, String planCode, String planName,
                       String status, Instant startsAt, Instant expiresAt, boolean active) {}
