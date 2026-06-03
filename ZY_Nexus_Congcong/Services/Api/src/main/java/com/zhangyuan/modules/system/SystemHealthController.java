package com.zhangyuan.modules.system;

import com.zhangyuan.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * 系统健康检查控制器，提供服务存活检测接口。
 */
@RestController
@RequestMapping("/api/system")
public class SystemHealthController {

    private static final Logger log = LoggerFactory.getLogger(SystemHealthController.class);

    /**
     * 健康检查接口，返回服务名称和当前时间。
     *
     * @return 包含服务信息的响应
     */
    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        log.info("Health check ping");
        return ApiResponse.ok(Map.of(
                "service", "zhangyuan-api",
                "time", Instant.now().toString()
        ));
    }
}
