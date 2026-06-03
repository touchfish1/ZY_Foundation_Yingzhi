package com.zhangyuan.modules.system.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.system.application.service.SystemSettingApplicationService;
import com.zhangyuan.modules.system.domain.model.SystemSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DDD 版系统设置控制器，提供系统设置的查询和更新接口。
 */
@RestController("dddSystemSettingController")
@RequestMapping("/api/ddd/settings")
public class SystemSettingController {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingController.class);

    private final SystemSettingApplicationService systemSettingApplicationService;

    public SystemSettingController(SystemSettingApplicationService systemSettingApplicationService) {
        this.systemSettingApplicationService = systemSettingApplicationService;
    }

    /**
     * 获取所有系统设置列表。
     *
     * @return 系统设置列表
     */
    @GetMapping
    public ApiResponse<List<SystemSetting>> list() {
        log.info("Listing system settings");
        return ApiResponse.ok(systemSettingApplicationService.listAll());
    }

    /**
     * 更新指定系统设置项的值。
     *
     * @param key  设置键
     * @param body 请求体，包含 value 字段
     * @return 更新后的设置项
     */
    @PutMapping("/{key}")
    public ApiResponse<SystemSetting> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        log.info("Updating system setting: key={}", key);
        return ApiResponse.ok(systemSettingApplicationService.update(key, body.get("value")));
    }
}
