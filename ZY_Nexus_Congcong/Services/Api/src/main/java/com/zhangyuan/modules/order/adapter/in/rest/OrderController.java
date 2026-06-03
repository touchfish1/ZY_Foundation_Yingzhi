package com.zhangyuan.modules.order.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.order.application.service.OrderApplicationService;
import com.zhangyuan.modules.order.domain.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DDD 版订单控制器，提供订单的创建和查询接口。
 */
@RestController("dddOrderController")
@RequestMapping("/api/ddd/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    /**
     * 创建新订单。
     *
     * @param request 创建订单请求
     * @return 创建的订单
     */
    @PostMapping
    public ApiResponse<Order> create(@RequestBody CreateOrderRequest request) {
        log.info("Creating order: planId={}, priceId={}, amount={}, currency={}", request.planId(), request.priceId(), request.amount(), request.currency());
        Order order = orderApplicationService.createOrder(
                request.planId(), request.priceId(), request.amount(), request.currency(), "{}");
        log.info("Order created: orderNo={}", order.getOrderNo());
        return ApiResponse.ok(order);
    }

    /**
     * 根据订单号查询订单。
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<Order> get(@PathVariable String orderNo) {
        log.info("Getting order: {}", orderNo);
        return orderApplicationService.findByOrderNo(orderNo)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error(404, "Order not found"));
    }

    /**
     * 获取所有订单列表。
     *
     * @return 订单列表
     */
    @GetMapping
    public ApiResponse<List<Order>> list() {
        log.info("Listing all orders");
        return ApiResponse.ok(orderApplicationService.listAll());
    }

    public record CreateOrderRequest(Long planId, Long priceId, BigDecimal amount, String currency) {}
}
