package com.zhangyuan.order.client;

import com.zhangyuan.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "zhangyuan-api", path = "/api")
public interface ProductServiceClient {

    @GetMapping("/products/plan-groups/{code}")
    ApiResponse<?> getPlanGroup(@PathVariable("code") String code);
}
