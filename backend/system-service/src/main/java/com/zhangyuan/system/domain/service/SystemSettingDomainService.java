package com.zhangyuan.system.domain.service;

import com.zhangyuan.system.domain.model.SystemSetting;
import com.zhangyuan.system.domain.repository.SystemSettingRepository;
import java.util.Map;

public class SystemSettingDomainService {

    public SystemSetting getOrCreate(SystemSettingRepository repository, String key) {
        return repository.findByKey(key)
                .orElseGet(() -> new SystemSetting(key, null));
    }

    public SystemSetting update(SystemSettingRepository repository, String key, String value) {
        SystemSetting setting = getOrCreate(repository, key);
        setting.updateValue(value);
        return repository.save(setting);
    }

    public void batchUpdate(SystemSettingRepository repository, Map<String, String> settings) {
        for (var entry : settings.entrySet()) {
            update(repository, entry.getKey(), entry.getValue());
        }
    }
}
