package com.zhangyuan.order.adapter.in.rest;

import com.zhangyuan.order.application.service.FulfillmentService;
import com.zhangyuan.order.application.service.OrderApplicationService;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.order.dto.CreateOrderRequest;
import com.zhangyuan.order.dto.OrderResponse;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("orderServiceOrderPublicController")
@RequestMapping("/api/orders")
public class OrderPublicController {

    private static final Logger log = LoggerFactory.getLogger(OrderPublicController.class);

    private final OrderApplicationService orderApplicationService;
    private final FulfillmentService fulfillmentService;

    public OrderPublicController(OrderApplicationService orderApplicationService,
                                 FulfillmentService fulfillmentService) {
        this.orderApplicationService = orderApplicationService;
        this.fulfillmentService = fulfillmentService;
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> list(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return ApiResponse.ok(orderApplicationService.listOrdersByUserId(userId));
        }
        return ApiResponse.ok(orderApplicationService.listOrders());
    }

    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        log.info("Creating order: planCode={}, billingCycle={}, currency={}, userId={}", request.planCode(), request.billingCycle(), request.currency(), userId);
        return ApiResponse.ok(orderApplicationService.createOrder(request, userId));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderResponse> get(@PathVariable String orderNo) {
        log.info("Getting order: {}", orderNo);
        return ApiResponse.ok(orderApplicationService.getOrder(orderNo));
    }

    @PostMapping("/{orderNo}/pay")
    public ApiResponse<OrderResponse> markPaid(@PathVariable String orderNo) {
        log.info("Marking order as paid: {}", orderNo);
        return ApiResponse.ok(orderApplicationService.toResponse(orderApplicationService.markPaid(orderNo)));
    }

    @PostMapping("/{orderNo}/fulfill")
    public ApiResponse<Void> fulfillOrder(@PathVariable String orderNo) {
        log.info("Fulfilling order: {}", orderNo);
        fulfillmentService.fulfillOrder(orderNo);
        return ApiResponse.ok();
    }
}
