package com.zhangyuan.payment.client;

import com.zhangyuan.payment.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "system-service", path = "/api/auth")
public interface UserServiceClient {

    @GetMapping("/verify-key")
    ApiResponse<Long> verifyApiKey(@RequestParam("apiKey") String apiKey);
}
