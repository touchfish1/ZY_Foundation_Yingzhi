package com.zhangyuan.modules.system.domain.repository;

import com.zhangyuan.modules.system.domain.model.SystemSetting;
import java.util.List;
import java.util.Optional;

public interface SystemSettingRepository {

    Optional<SystemSetting> findByKey(String key);

    List<SystemSetting> findAllOrderByKeyAsc();

    SystemSetting save(SystemSetting setting);
}
