package com.zhangyuan.modules.system.adapter.out.persistence;

import com.zhangyuan.modules.system.domain.model.SystemSetting;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaSystemSettingRepositoryTest {

    private final com.zhangyuan.modules.system.repository.SystemSettingRepository jpaRepository =
            mock(com.zhangyuan.modules.system.repository.SystemSettingRepository.class);
    private final JpaSystemSettingRepository repository = new JpaSystemSettingRepository(jpaRepository);

    @Test
    void findByKeyDelegates() {
        com.zhangyuan.modules.system.domain.SystemSetting entity = new com.zhangyuan.modules.system.domain.SystemSetting(
                "site_name", "ZHANGYUAN");
        when(jpaRepository.findBySettingKey("site_name")).thenReturn(Optional.of(entity));

        Optional<SystemSetting> result = repository.findByKey("site_name");

        assertThat(result).isPresent();
        assertThat(result.get().getValue()).isEqualTo("ZHANGYUAN");
    }

    @Test
    void saveNewSetting() {
        SystemSetting setting = new SystemSetting("new_key", "value");
        com.zhangyuan.modules.system.domain.SystemSetting savedEntity = new com.zhangyuan.modules.system.domain.SystemSetting(
                "new_key", "value");
        when(jpaRepository.save(any())).thenReturn(savedEntity);

        SystemSetting result = repository.save(setting);

        assertThat(result.getKey()).isEqualTo("new_key");
        verify(jpaRepository).save(any());
    }
}
