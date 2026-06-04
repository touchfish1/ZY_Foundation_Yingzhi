package com.zhangyuan.system.adapter.in.rest;

import com.zhangyuan.system.common.ApiResponse;
import com.zhangyuan.system.application.service.SystemSettingApplicationService;
import com.zhangyuan.system.domain.model.SystemSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController("dddSystemSettingController")
@RequestMapping("/api/ddd/settings")
public class SystemSettingController {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingController.class);

    private final SystemSettingApplicationService systemSettingApplicationService;

    public SystemSettingController(SystemSettingApplicationService systemSettingApplicationService) {
        this.systemSettingApplicationService = systemSettingApplicationService;
    }

    @GetMapping
    public ApiResponse<List<SystemSetting>> list() {
        log.info("Listing system settings");
        return ApiResponse.ok(systemSettingApplicationService.listAll());
    }

    @PutMapping("/{key}")
    public ApiResponse<SystemSetting> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        log.info("Updating system setting: key={}", key);
        return ApiResponse.ok(systemSettingApplicationService.update(key, body.get("value")));
    }
}
