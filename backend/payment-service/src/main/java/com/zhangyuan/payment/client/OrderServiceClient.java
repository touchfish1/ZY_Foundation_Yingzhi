package com.zhangyuan.payment.client;

import com.zhangyuan.payment.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", path = "/api/orders")
public interface OrderServiceClient {
    @GetMapping("/{orderNo}")
    ApiResponse<?> getOrder(@PathVariable("orderNo") String orderNo);
}
