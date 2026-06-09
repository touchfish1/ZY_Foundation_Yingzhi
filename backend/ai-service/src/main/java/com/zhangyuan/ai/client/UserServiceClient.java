package com.zhangyuan.ai.client;

import com.zhangyuan.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "user-service", path = "/api/auth")
public interface UserServiceClient {

    @GetMapping("/verify-key-with-quota")
    ApiResponse<Map<String, Object>> verifyApiKey(@RequestParam("apiKey") String apiKey);
}
