package com.zhangyuan.modules.order;

import com.zhangyuan.common.response.ApiResponse;
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
 * 订单控制器，提供订单创建和查询接口。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建新订单。
     *
     * @param request 创建订单请求
     * @return 订单响应
     */
    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order: planCode={}, billingCycle={}, currency={}", request.planCode(), request.billingCycle(), request.currency());
        return ApiResponse.ok(orderService.create(request));
    }

    /**
     * 根据订单号查询订单。
     *
     * @param orderNo 订单号
     * @return 订单响应
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderResponse> get(@PathVariable String orderNo) {
        log.info("Getting order: {}", orderNo);
        return ApiResponse.ok(orderService.get(orderNo));
    }
}
