package com.zhangyuan.modules.system.application.service;

import com.zhangyuan.modules.system.domain.model.SystemSetting;
import com.zhangyuan.modules.system.domain.repository.SystemSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 系统设置应用服务，负责设置项的查询与更新编排。
 */
@Service
public class SystemSettingApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingApplicationService.class);

    private final SystemSettingRepository systemSettingRepository;

    public SystemSettingApplicationService(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    /**
     * 获取所有设置项。
     *
     * @return 设置项列表
     */
    @Transactional(readOnly = true)
    public List<SystemSetting> listAll() {
        return systemSettingRepository.findAllOrderByKeyAsc();
    }

    /**
     * 获取公开的设置项映射。
     *
     * @return 设置键值对映射
     */
    @Transactional(readOnly = true)
    public Map<String, String> getPublicMap() {
        return listAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        SystemSetting::getKey,
                        SystemSetting::getValue,
                        (a, b) -> b));
    }

    /**
     * 更新或创建指定设置项。
     *
     * @param key   设置键
     * @param value 设置值
     * @return 更新后的设置项
     */
    @Transactional
    public SystemSetting update(String key, String value) {
        log.info("Updating system setting: key={}", key);
        SystemSetting setting = systemSettingRepository.findByKey(key)
                .orElse(new SystemSetting(key, null));
        setting.updateValue(value);
        SystemSetting saved = systemSettingRepository.save(setting);
        log.info("System setting updated: key={}", key);
        return saved;
    }

    /**
     * 批量更新设置项。
     *
     * @param settings 设置键值对映射
     * @return 更新后的设置项列表
     */
    @Transactional
    public List<SystemSetting> batchUpdate(Map<String, String> settings) {
        log.info("Batch updating system settings: count={}", settings.size());
        for (var entry : settings.entrySet()) {
            update(entry.getKey(), entry.getValue());
        }
        return listAll();
    }
}
