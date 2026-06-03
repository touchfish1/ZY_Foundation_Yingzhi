package com.zhangyuan.modules.system;

import com.zhangyuan.modules.system.dto.BatchUpdateRequest;
import com.zhangyuan.modules.system.dto.SettingResponse;
import com.zhangyuan.modules.system.dto.UpdateSettingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台系统设置管理控制器，提供设置项的查看、更新和批量更新接口。
 */
@RestController
@RequestMapping("/admin/system/settings")
public class SystemSettingAdminController {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingAdminController.class);

    private final SystemSettingService service;

    public SystemSettingAdminController(SystemSettingService service) {
        this.service = service;
    }

    /**
     * 获取所有系统设置列表。
     *
     * @return 设置项列表
     */
    @GetMapping
    public List<SettingResponse> listSettings() {
        log.info("Listing system settings");
        return service.listSettings();
    }

    /**
     * 更新指定系统设置项的值。
     *
     * @param key     设置键
     * @param request 更新请求体
     * @return 更新后的设置项
     */
    @PutMapping("/{key}")
    public SettingResponse updateSetting(@PathVariable String key, @RequestBody UpdateSettingRequest request) {
        log.info("Updating system setting: key={}", key);
        return service.updateSetting(key, request);
    }

    /**
     * 批量更新系统设置项。
     *
     * @param request 批量更新请求体
     * @return 更新后的设置项列表
     */
    @PutMapping
    public List<SettingResponse> batchUpdate(@RequestBody BatchUpdateRequest request) {
        log.info("Batch updating system settings: count={}", request.settings().size());
        return service.batchUpdate(request);
    }
}
