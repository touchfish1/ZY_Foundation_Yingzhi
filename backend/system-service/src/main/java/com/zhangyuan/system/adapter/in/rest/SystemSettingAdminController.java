package com.zhangyuan.system.adapter.in.rest;

import com.zhangyuan.system.application.service.SystemSettingApplicationService;
import com.zhangyuan.system.dto.BatchUpdateRequest;
import com.zhangyuan.system.dto.SettingResponse;
import com.zhangyuan.system.dto.UpdateSettingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/system/settings")
public class SystemSettingAdminController {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingAdminController.class);

    private final SystemSettingApplicationService service;

    public SystemSettingAdminController(SystemSettingApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<SettingResponse> listSettings() {
        log.info("Listing system settings");
        return service.listAll().stream()
                .map(s -> new SettingResponse(s.getKey(), s.getValue()))
                .toList();
    }

    @PutMapping("/{key}")
    public SettingResponse updateSetting(@PathVariable String key, @RequestBody UpdateSettingRequest request) {
        log.info("Updating system setting: key={}", key);
        var setting = service.update(key, request.value());
        return new SettingResponse(setting.getKey(), setting.getValue());
    }

    @PutMapping
    public List<SettingResponse> batchUpdate(@RequestBody BatchUpdateRequest request) {
        log.info("Batch updating system settings: count={}", request.settings().size());
        return service.batchUpdate(request.settings()).stream()
                .map(s -> new SettingResponse(s.getKey(), s.getValue()))
                .toList();
    }
}
