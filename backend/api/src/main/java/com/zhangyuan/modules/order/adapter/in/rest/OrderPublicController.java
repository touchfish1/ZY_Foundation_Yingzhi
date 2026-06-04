package com.zhangyuan.modules.order.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.order.application.service.OrderApplicationService;
import com.zhangyuan.modules.order.dto.CreateOrderRequest;
import com.zhangyuan.modules.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单公开控制器，提供订单创建和查询接口。
 */
@RestController("orderPublicController")
@RequestMapping("/api/orders")
public class OrderPublicController {

    private static final Logger log = LoggerFactory.getLogger(OrderPublicController.class);

    private final OrderApplicationService orderApplicationService;

    public OrderPublicController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    /**
     * 创建新订单。
     */
    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order: planCode={}, billingCycle={}, currency={}", request.planCode(), request.billingCycle(), request.currency());
        return ApiResponse.ok(orderApplicationService.createOrder(request));
    }

    /**
     * 根据订单号查询订单。
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderResponse> get(@PathVariable String orderNo) {
        log.info("Getting order: {}", orderNo);
        return ApiResponse.ok(orderApplicationService.getOrder(orderNo));
    }
}
