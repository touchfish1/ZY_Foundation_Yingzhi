package com.zhangyuan.modules.system.domain.service;

import com.zhangyuan.modules.system.domain.model.SystemSetting;
import com.zhangyuan.modules.system.domain.repository.SystemSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统设置领域服务，包含设置项的核心业务逻辑。
 */
@Service
public class SystemSettingDomainService {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingDomainService.class);

    /**
     * 获取或创建设置项。
     *
     * @param repository 设置项仓库
     * @param key        设置键
     * @return 设置项领域对象
     */
    public SystemSetting getOrCreate(SystemSettingRepository repository, String key) {
        return repository.findByKey(key)
                .orElseGet(() -> {
                    log.info("Creating new system setting: key={}", key);
                    return new SystemSetting(key, null);
                });
    }

    /**
     * 批量更新设置项。
     *
     * @param repository 设置项仓库
     * @param settings   设置键值对映射
     */
    public void batchUpdate(SystemSettingRepository repository, java.util.Map<String, String> settings) {
        log.info("Domain batch updating system settings: count={}", settings.size());
        for (var entry : settings.entrySet()) {
            SystemSetting setting = getOrCreate(repository, entry.getKey());
            setting.updateValue(entry.getValue());
            repository.save(setting);
        }
    }
}
