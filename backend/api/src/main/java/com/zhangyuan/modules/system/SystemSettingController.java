package com.zhangyuan.modules.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 公开系统设置控制器，提供前台可读取的系统设置接口。
 */
@RestController
@RequestMapping("/api/system/settings")
public class SystemSettingController {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingController.class);

    private final SystemSettingService service;

    public SystemSettingController(SystemSettingService service) {
        this.service = service;
    }

    /**
     * 获取公开的系统设置（键值对形式）。
     *
     * @return 设置键值对映射
     */
    @GetMapping
    public Map<String, String> getSettings() {
        log.info("Getting public system settings");
        return service.getPublicSettings();
    }
}
