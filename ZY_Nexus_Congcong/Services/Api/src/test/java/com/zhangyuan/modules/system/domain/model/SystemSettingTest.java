package com.zhangyuan.modules.system.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SystemSettingTest {

    @Test
    void createSetting() {
        SystemSetting setting = new SystemSetting("site_name", "ZHANGYUAN");
        assertThat(setting.getKey()).isEqualTo("site_name");
        assertThat(setting.getValue()).isEqualTo("ZHANGYUAN");
    }

    @Test
    void updateValue() {
        SystemSetting setting = new SystemSetting("site_name", "old");
        setting.updateValue("new");
        assertThat(setting.getValue()).isEqualTo("new");
    }
}
