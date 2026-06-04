package com.zhangyuan.system.application.service;

import com.zhangyuan.system.domain.model.SystemSetting;
import com.zhangyuan.system.domain.repository.SystemSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SystemSettingApplicationServiceTest {

    private final SystemSettingRepository settingRepository = mock(SystemSettingRepository.class);
    private SystemSettingApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SystemSettingApplicationService(settingRepository);
    }

    @Test
    void listAll_returnsAllSettings() {
        when(settingRepository.findAllOrderByKeyAsc()).thenReturn(List.of(
                new SystemSetting("site_name", "ZHANGYUAN"),
                new SystemSetting("site_desc", "API Platform")
        ));
        var result = service.listAll();
        assertThat(result).hasSize(2);
    }

    @Test
    void getPublicMap_returnsKeyValuePairs() {
        when(settingRepository.findAllOrderByKeyAsc()).thenReturn(List.of(
                new SystemSetting("site_name", "ZHANGYUAN"),
                new SystemSetting("site_desc", "API Platform")
        ));
        var result = service.getPublicMap();
        assertThat(result)
                .hasSize(2)
                .containsEntry("site_name", "ZHANGYUAN")
                .containsEntry("site_desc", "API Platform");
    }

    @Test
    void getPublicMap_empty_returnsEmptyMap() {
        when(settingRepository.findAllOrderByKeyAsc()).thenReturn(List.of());
        assertThat(service.getPublicMap()).isEmpty();
    }

    @Test
    void update_createsNew() {
        when(settingRepository.findByKey("new_key")).thenReturn(java.util.Optional.empty());
        when(settingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.update("new_key", "new_value");

        verify(settingRepository).save(argThat(s -> "new_key".equals(s.getKey()) && "new_value".equals(s.getValue())));
    }

    @Test
    void update_updatesExisting() {
        SystemSetting existing = new SystemSetting("site_name", "old");
        when(settingRepository.findByKey("site_name")).thenReturn(java.util.Optional.of(existing));
        when(settingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.update("site_name", "new");

        verify(settingRepository).save(argThat(s -> s.getValue().equals("new")));
    }

    @Test
    void batchUpdate_updatesAll() {
        when(settingRepository.findByKey(anyString())).thenReturn(java.util.Optional.empty());
        when(settingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.batchUpdate(Map.of("k1", "v1", "k2", "v2"));

        verify(settingRepository, times(2)).save(any());
    }

    @Test
    void listAll_noSettings_returnsEmpty() {
        when(settingRepository.findAllOrderByKeyAsc()).thenReturn(List.of());
        assertThat(service.listAll()).isEmpty();
    }
}
