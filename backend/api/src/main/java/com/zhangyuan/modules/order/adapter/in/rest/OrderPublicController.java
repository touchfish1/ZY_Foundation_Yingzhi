package com.zhangyuan.modules.order.adapter.in.rest;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.common.operationlog.annotation.OperationLog;
import static com.zhangyuan.common.operationlog.domain.model.OperationType.*;
import static com.zhangyuan.common.operationlog.domain.model.ResourceType.*;
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
@RestController
@SaIgnore
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
    @OperationLog(type = CREATE, resource = ORDER)
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        log.info("Creating order: planCode={}, billingCycle={}, currency={}, userId={}", request.planCode(), request.billingCycle(), request.currency(), userId);
        return ApiResponse.ok(orderApplicationService.createOrder(request, userId));
    }

    /**
     * 根据订单号查询订单。
     */
    @GetMapping("/{orderNo}")
    @OperationLog(type = QUERY, resource = ORDER, resourceId = "#orderNo")
    public ApiResponse<OrderResponse> get(@PathVariable String orderNo) {
        log.info("Getting order: {}", orderNo);
        return ApiResponse.ok(orderApplicationService.getOrder(orderNo));
    }
}
