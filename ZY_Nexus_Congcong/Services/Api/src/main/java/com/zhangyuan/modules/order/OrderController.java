package com.zhangyuan.modules.order;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.order.dto.CreateOrderRequest;
import com.zhangyuan.modules.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderService.create(request));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderResponse> get(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.get(orderNo));
    }
}
