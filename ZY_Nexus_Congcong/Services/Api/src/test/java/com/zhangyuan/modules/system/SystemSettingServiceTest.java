package com.zhangyuan.modules.system;

import com.zhangyuan.modules.system.domain.SystemSetting;
import com.zhangyuan.modules.system.dto.BatchUpdateRequest;
import com.zhangyuan.modules.system.dto.SettingResponse;
import com.zhangyuan.modules.system.dto.UpdateSettingRequest;
import com.zhangyuan.modules.system.repository.SystemSettingRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSettingServiceTest {

    private final SystemSettingRepository repository = mock(SystemSettingRepository.class);
    private final SystemSettingService service = new SystemSettingService(repository);

    @Test
    void listSettingsReturnsAll() {
        when(repository.findAllByOrderBySettingKeyAsc()).thenReturn(List.of(
                new SystemSetting("site_name", "ZHANGYUAN"),
                new SystemSetting("site_description", "API Platform")
        ));

        List<SettingResponse> settings = service.listSettings();

        assertThat(settings).hasSize(2);
        assertThat(settings.getFirst().key()).isEqualTo("site_name");
        assertThat(settings.getFirst().value()).isEqualTo("ZHANGYUAN");
    }

    @Test
    void getPublicSettingsReturnsMap() {
        when(repository.findAllByOrderBySettingKeyAsc()).thenReturn(List.of(
                new SystemSetting("site_name", "ZHANGYUAN"),
                new SystemSetting("site_description", "API Platform")
        ));

        Map<String, String> result = service.getPublicSettings();

        assertThat(result)
                .hasSize(2)
                .containsEntry("site_name", "ZHANGYUAN")
                .containsEntry("site_description", "API Platform");
    }

    @Test
    void updateSettingExistingUpdatesValue() {
        SystemSetting setting = new SystemSetting("site_name", "old");
        when(repository.findBySettingKey("site_name")).thenReturn(Optional.of(setting));

        SettingResponse response = service.updateSetting("site_name", new UpdateSettingRequest("ZHANGYUAN"));

        assertThat(response.value()).isEqualTo("ZHANGYUAN");
    }

    @Test
    void updateSettingNewCreatesAndUpdates() {
        when(repository.findBySettingKey("new_key")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(new SystemSetting("new_key", null));

        SettingResponse response = service.updateSetting("new_key", new UpdateSettingRequest("value"));

        assertThat(response.key()).isEqualTo("new_key");
        assertThat(response.value()).isEqualTo("value");
        verify(repository).save(any());
    }

    @Test
    void batchUpdateUpdatesMultiple() {
        SystemSetting existing = new SystemSetting("site_name", "old");
        when(repository.findBySettingKey("site_name")).thenReturn(Optional.of(existing));
        when(repository.findBySettingKey("new_key")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(new SystemSetting("new_key", null));
        when(repository.findAllByOrderBySettingKeyAsc()).thenReturn(List.of(
                new SystemSetting("new_key", "new_value"),
                new SystemSetting("site_name", "ZHANGYUAN")
        ));

        BatchUpdateRequest request = new BatchUpdateRequest(Map.of(
                "site_name", "ZHANGYUAN",
                "new_key", "new_value"
        ));
        List<SettingResponse> results = service.batchUpdate(request);

        assertThat(results).hasSize(2);
        verify(repository).save(any());
    }
}
