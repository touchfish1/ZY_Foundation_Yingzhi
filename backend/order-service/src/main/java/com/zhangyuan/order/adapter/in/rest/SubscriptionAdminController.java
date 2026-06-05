package com.zhangyuan.order.adapter.in.rest;

import com.zhangyuan.order.application.service.SubscriptionQueryService;
import com.zhangyuan.order.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/admin/subscriptions")
public class SubscriptionAdminController {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionAdminController.class);
    private final SubscriptionQueryService subscriptionQueryService;

    public SubscriptionAdminController(SubscriptionQueryService subscriptionQueryService) {
        this.subscriptionQueryService = subscriptionQueryService;
    }

    @GetMapping
    public ApiResponse<List<AdminSubscriptionDTO>> listAll() {
        log.info("Admin listing all subscriptions");
        var list = subscriptionQueryService.listAll().stream()
                .map(sub -> new AdminSubscriptionDTO(sub.getId(), sub.getUserId(), sub.getPlanCode(),
                        sub.getPlanName(), sub.getStatus(), sub.getStartsAt(), sub.getExpiresAt(),
                        sub.getCreatedAt(), sub.isActive()))
                .toList();
        return ApiResponse.ok(list);
    }
}

record AdminSubscriptionDTO(Long id, Long userId, String planCode, String planName,
                            String status, Instant startsAt, Instant expiresAt,
                            Instant createdAt, boolean active) {}
