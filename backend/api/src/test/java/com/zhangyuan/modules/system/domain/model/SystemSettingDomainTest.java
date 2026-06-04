package com.zhangyuan.modules.system.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SystemSettingDomainTest {

    @Test
    void create() {
        SystemSetting s = new SystemSetting("site_name", "ZHANGYUAN");
        assertThat(s.getKey()).isEqualTo("site_name");
        assertThat(s.getValue()).isEqualTo("ZHANGYUAN");
    }

    @Test
    void updateValue() {
        SystemSetting s = new SystemSetting("site_name", "old");
        s.updateValue("new");
        assertThat(s.getValue()).isEqualTo("new");
    }
}
