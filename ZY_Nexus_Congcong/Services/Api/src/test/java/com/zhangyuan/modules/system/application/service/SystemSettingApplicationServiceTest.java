package com.zhangyuan.modules.system.application.service;

import com.zhangyuan.modules.system.domain.model.SystemSetting;
import com.zhangyuan.modules.system.domain.repository.SystemSettingRepository;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSettingApplicationServiceTest {

    private final SystemSettingRepository repository = mock(SystemSettingRepository.class);
    private final SystemSettingApplicationService service = new SystemSettingApplicationService(repository);

    @Test
    void updateExistingSaves() {
        SystemSetting existing = new SystemSetting("site_name", "old");
        when(repository.findByKey("site_name")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SystemSetting result = service.update("site_name", "new");

        assertThat(result.getValue()).isEqualTo("new");
        verify(repository).save(any());
    }

    @Test
    void updateNewCreatesAndSaves() {
        when(repository.findByKey("new_key")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SystemSetting result = service.update("new_key", "value");

        assertThat(result.getValue()).isEqualTo("value");
        verify(repository).save(any());
    }

    @Test
    void listAllDelegates() {
        service.listAll();
        verify(repository).findAllOrderByKeyAsc();
    }

    @Test
    void batchUpdateUpdatesAll() {
        when(repository.findByKey("k1")).thenReturn(Optional.of(new SystemSetting("k1", "old")));
        when(repository.findByKey("k2")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findAllOrderByKeyAsc()).thenReturn(java.util.List.of());

        var result = service.batchUpdate(Map.of("k1", "v1", "k2", "v2"));

        assertThat(result).isNotNull();
        verify(repository, times(2)).save(any());
    }
}
