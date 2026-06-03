package com.zhangyuan.modules.system;

import com.zhangyuan.modules.system.domain.SystemSetting;
import com.zhangyuan.modules.system.dto.BatchUpdateRequest;
import com.zhangyuan.modules.system.dto.SettingResponse;
import com.zhangyuan.modules.system.dto.UpdateSettingRequest;
import com.zhangyuan.modules.system.repository.SystemSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务，提供设置的增删改查功能。
 */
@Service
public class SystemSettingService {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingService.class);

    private final SystemSettingRepository repository;

    public SystemSettingService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    /**
     * 获取所有设置项列表。
     *
     * @return 设置项响应列表
     */
    @Transactional(readOnly = true)
    public List<SettingResponse> listSettings() {
        return repository.findAllByOrderBySettingKeyAsc().stream()
                .map(s -> new SettingResponse(s.getSettingKey(), s.getSettingValue()))
                .toList();
    }

    /**
     * 获取公开的设置项，以键值对形式返回。
     *
     * @return 设置键值对映射
     */
    @Transactional(readOnly = true)
    public Map<String, String> getPublicSettings() {
        Map<String, String> map = new LinkedHashMap<>();
        for (SystemSetting s : repository.findAllByOrderBySettingKeyAsc()) {
            map.put(s.getSettingKey(), s.getSettingValue());
        }
        return map;
    }

    /**
     * 更新或创建指定设置项。
     *
     * @param key     设置键
     * @param request 更新请求
     * @return 更新后的设置项
     */
    @Transactional
    public SettingResponse updateSetting(String key, UpdateSettingRequest request) {
        log.info("Updating system setting: key={}", key);
        // 查找设置项，不存在则新建
        SystemSetting setting = repository.findBySettingKey(key)
                .orElseGet(() -> repository.save(new SystemSetting(key, null)));
        setting.updateValue(request.value());
        log.info("System setting updated: key={}", key);
        return new SettingResponse(setting.getSettingKey(), setting.getSettingValue());
    }

    /**
     * 批量更新或创建设置项。
     *
     * @param request 批量更新请求
     * @return 更新后的设置项列表
     */
    @Transactional
    public List<SettingResponse> batchUpdate(BatchUpdateRequest request) {
        log.info("Batch updating system settings: count={}", request.settings().size());
        for (Map.Entry<String, String> entry : request.settings().entrySet()) {
            // 逐个查找并更新设置项
            SystemSetting setting = repository.findBySettingKey(entry.getKey())
                    .orElseGet(() -> repository.save(new SystemSetting(entry.getKey(), null)));
            setting.updateValue(entry.getValue());
        }
        return listSettings();
    }
}
