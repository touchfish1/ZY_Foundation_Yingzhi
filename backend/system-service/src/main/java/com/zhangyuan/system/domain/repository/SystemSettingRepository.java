package com.zhangyuan.system.domain.repository;

import com.zhangyuan.system.domain.model.SystemSetting;
import java.util.List;
import java.util.Optional;

public interface SystemSettingRepository {

    Optional<SystemSetting> findByKey(String key);

    List<SystemSetting> findAllOrderByKeyAsc();

    SystemSetting save(SystemSetting setting);
}
