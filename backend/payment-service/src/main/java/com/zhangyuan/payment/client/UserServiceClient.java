package com.zhangyuan.payment.client;

import java.util.Map;

import com.zhangyuan.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", path = "/api/auth")
public interface UserServiceClient {

    @GetMapping("/verify-key")
    ApiResponse<Long> verifyApiKey(@RequestParam("apiKey") String apiKey);

    @GetMapping("/verify-key-with-quota")
    ApiResponse<Map<String, Object>> verifyApiKeyWithQuota(@RequestParam("apiKey") String apiKey);
}
