package com.zhangyuan.modules.order.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.order.application.service.OrderApplicationService;
import com.zhangyuan.modules.order.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台订单管理控制器，提供订单列表查询接口。
 */
@RestController("dddOrderAdminController")
@RequestMapping("/admin/orders")
@SaCheckPermission("order:list")
public class OrderAdminController {

    private static final Logger log = LoggerFactory.getLogger(OrderAdminController.class);

    private final OrderApplicationService orderApplicationService;

    public OrderAdminController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    /**
     * 获取订单列表（分页）。
     */
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("Listing orders, page={}, pageSize={}", page, pageSize);
        return ApiResponse.ok(orderApplicationService.listOrders(page, pageSize));
    }
}
