package com.zhangyuan.order.adapter.in.rest;

import com.zhangyuan.order.application.service.FulfillmentService;
import com.zhangyuan.order.application.service.OrderApplicationService;
import com.zhangyuan.order.common.ApiResponse;
import com.zhangyuan.order.dto.CreateOrderRequest;
import com.zhangyuan.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order: planCode={}, billingCycle={}, currency={}", request.planCode(), request.billingCycle(), request.currency());
        return ApiResponse.ok(orderApplicationService.createOrder(request));
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
