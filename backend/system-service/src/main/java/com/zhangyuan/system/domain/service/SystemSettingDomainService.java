package com.zhangyuan.system.domain.service;

import com.zhangyuan.system.domain.model.SystemSetting;
import com.zhangyuan.system.domain.repository.SystemSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SystemSettingDomainService {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingDomainService.class);

    public SystemSetting getOrCreate(SystemSettingRepository repository, String key) {
        return repository.findByKey(key)
                .orElseGet(() -> {
                    log.info("Creating new system setting: key={}", key);
                    return new SystemSetting(key, null);
                });
    }

    public void batchUpdate(SystemSettingRepository repository, Map<String, String> settings) {
        log.info("Domain batch updating system settings: count={}", settings.size());
        for (var entry : settings.entrySet()) {
            SystemSetting setting = getOrCreate(repository, entry.getKey());
            setting.updateValue(entry.getValue());
            repository.save(setting);
        }
    }
}
