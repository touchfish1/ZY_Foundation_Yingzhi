package com.zhangyuan.modules.system.adapter.out.persistence;

import com.zhangyuan.modules.system.domain.model.SystemSetting;
import com.zhangyuan.modules.system.domain.repository.SystemSettingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaSystemSettingRepository implements SystemSettingRepository {

    private final com.zhangyuan.modules.system.repository.SystemSettingRepository jpaRepository;

    public JpaSystemSettingRepository(com.zhangyuan.modules.system.repository.SystemSettingRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SystemSetting> findByKey(String key) {
        return jpaRepository.findBySettingKey(key).map(this::toDomain);
    }

    @Override
    public List<SystemSetting> findAllOrderByKeyAsc() {
        return jpaRepository.findAllByOrderBySettingKeyAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public SystemSetting save(SystemSetting setting) {
        com.zhangyuan.modules.system.domain.SystemSetting entity =
                jpaRepository.findBySettingKey(setting.getKey())
                        .orElseGet(() -> new com.zhangyuan.modules.system.domain.SystemSetting(
                                setting.getKey(), setting.getValue()));
        entity.updateValue(setting.getValue());
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private SystemSetting toDomain(com.zhangyuan.modules.system.domain.SystemSetting entity) {
        return new SystemSetting(entity.getSettingKey(), entity.getSettingValue());
    }
}
