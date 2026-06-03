package com.zhangyuan.modules.system;

import com.zhangyuan.modules.system.dto.BatchUpdateRequest;
import com.zhangyuan.modules.system.dto.SettingResponse;
import com.zhangyuan.modules.system.dto.UpdateSettingRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSettingAdminControllerTest {

    private final SystemSettingService service = mock(SystemSettingService.class);
    private final SystemSettingAdminController controller = new SystemSettingAdminController(service);

    @Test
    void listSettingsDelegates() {
        SettingResponse setting = new SettingResponse("site_name", "ZHANGYUAN");
        when(service.listSettings()).thenReturn(List.of(setting));

        List<SettingResponse> result = controller.listSettings();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().key()).isEqualTo("site_name");
        verify(service).listSettings();
    }

    @Test
    void updateSettingDelegates() {
        UpdateSettingRequest request = new UpdateSettingRequest("new_value");
        SettingResponse expected = new SettingResponse("site_name", "new_value");
        when(service.updateSetting("site_name", request)).thenReturn(expected);

        SettingResponse result = controller.updateSetting("site_name", request);

        assertThat(result.key()).isEqualTo("site_name");
        assertThat(result.value()).isEqualTo("new_value");
        verify(service).updateSetting("site_name", request);
    }

    @Test
    void batchUpdateDelegates() {
        BatchUpdateRequest request = new BatchUpdateRequest(Map.of("key1", "val1", "key2", "val2"));
        when(service.batchUpdate(request)).thenReturn(List.of(
                new SettingResponse("key1", "val1"),
                new SettingResponse("key2", "val2")
        ));

        List<SettingResponse> result = controller.batchUpdate(request);

        assertThat(result).hasSize(2);
        verify(service).batchUpdate(request);
    }
}
