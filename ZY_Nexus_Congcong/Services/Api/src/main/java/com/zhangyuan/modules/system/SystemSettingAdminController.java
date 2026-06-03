package com.zhangyuan.modules.system;

import com.zhangyuan.modules.system.dto.BatchUpdateRequest;
import com.zhangyuan.modules.system.dto.SettingResponse;
import com.zhangyuan.modules.system.dto.UpdateSettingRequest;
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

    private final SystemSettingService service;

    public SystemSettingAdminController(SystemSettingService service) {
        this.service = service;
    }

    @GetMapping
    public List<SettingResponse> listSettings() {
        return service.listSettings();
    }

    @PutMapping("/{key}")
    public SettingResponse updateSetting(@PathVariable String key, @RequestBody UpdateSettingRequest request) {
        return service.updateSetting(key, request);
    }

    @PutMapping
    public List<SettingResponse> batchUpdate(@RequestBody BatchUpdateRequest request) {
        return service.batchUpdate(request);
    }
}
