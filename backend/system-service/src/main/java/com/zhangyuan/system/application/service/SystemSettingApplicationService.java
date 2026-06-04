package com.zhangyuan.system.application.service;

import com.zhangyuan.system.domain.model.SystemSetting;
import com.zhangyuan.system.domain.repository.SystemSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SystemSettingApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingApplicationService.class);

    private final SystemSettingRepository systemSettingRepository;

    public SystemSettingApplicationService(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    @Transactional(readOnly = true)
    public List<SystemSetting> listAll() {
        return systemSettingRepository.findAllOrderByKeyAsc();
    }

    @Transactional(readOnly = true)
    public Map<String, String> getPublicMap() {
        return listAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        SystemSetting::getKey,
                        SystemSetting::getValue,
                        (a, b) -> b));
    }

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

    @Transactional
    public List<SystemSetting> batchUpdate(Map<String, String> settings) {
        log.info("Batch updating system settings: count={}", settings.size());
        for (var entry : settings.entrySet()) {
            update(entry.getKey(), entry.getValue());
        }
        return listAll();
    }
}
