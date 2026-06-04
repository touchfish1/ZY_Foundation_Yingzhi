package com.zhangyuan.system.adapter.out.persistence;

import com.zhangyuan.system.domain.model.SystemSetting;
import com.zhangyuan.system.domain.repository.SystemSettingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaSystemSettingRepository implements SystemSettingRepository {

    private final SystemSettingJpaRepository jpaRepository;

    public JpaSystemSettingRepository(SystemSettingJpaRepository jpaRepository) {
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
        SystemSettingEntity entity =
                jpaRepository.findBySettingKey(setting.getKey())
                        .orElseGet(() -> new SystemSettingEntity(
                                setting.getKey(), setting.getValue()));
        entity.updateValue(setting.getValue());
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private SystemSetting toDomain(SystemSettingEntity entity) {
        return new SystemSetting(entity.getSettingKey(), entity.getSettingValue());
    }
}
