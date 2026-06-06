package com.zhangyuan.payment.client;

import com.zhangyuan.payment.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-service", path = "/api/orders", contextId = "fulfillmentClient")
public interface FulfillmentClient {

    @PostMapping("/{orderNo}/pay")
    ApiResponse<?> markPaid(@PathVariable("orderNo") String orderNo);

    @PostMapping("/{orderNo}/fulfill")
    ApiResponse<Void> fulfillOrder(@PathVariable("orderNo") String orderNo);
}
