package com.zhangyuan.modules.system;

import com.zhangyuan.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemHealthController {

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.ok(Map.of(
                "service", "zhangyuan-api",
                "time", Instant.now().toString()
        ));
    }
}
