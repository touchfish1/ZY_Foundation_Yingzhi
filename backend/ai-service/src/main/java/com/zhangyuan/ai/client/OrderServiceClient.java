package com.zhangyuan.ai.client;

import com.zhangyuan.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "order-service", path = "/api/usage")
public interface OrderServiceClient {

    @PostMapping("/record")
    ApiResponse<Void> recordUsage(@RequestBody Map<String, Object> usageRecord);
}
