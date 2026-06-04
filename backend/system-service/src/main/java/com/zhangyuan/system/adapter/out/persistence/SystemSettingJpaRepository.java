package com.zhangyuan.system.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemSettingJpaRepository extends JpaRepository<SystemSettingEntity, Long> {

    Optional<SystemSettingEntity> findBySettingKey(String settingKey);

    List<SystemSettingEntity> findAllByOrderBySettingKeyAsc();
}
