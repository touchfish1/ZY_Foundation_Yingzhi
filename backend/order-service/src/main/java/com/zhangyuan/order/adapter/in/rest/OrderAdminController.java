package com.zhangyuan.order.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.order.application.service.OrderApplicationService;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.order.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("orderServiceOrderAdminController")
@RequestMapping("/admin/orders")
@SaCheckPermission("order:list")
public class OrderAdminController {

    private static final Logger log = LoggerFactory.getLogger(OrderAdminController.class);

    private final OrderApplicationService orderApplicationService;

    public OrderAdminController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> listOrders() {
        log.info("Listing all orders");
        return ApiResponse.ok(orderApplicationService.listOrders());
    }
}
