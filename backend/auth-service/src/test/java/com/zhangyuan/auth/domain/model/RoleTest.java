package com.zhangyuan.auth.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RoleTest {

    @Test
    void createRole_setsCodeAndName() {
        Role role = new Role("admin", "Administrator");
        assertThat(role.getCode()).isEqualTo("admin");
        assertThat(role.getName()).isEqualTo("Administrator");
        assertThat(role.getCreatedAt()).isNotNull();
    }

    @Test
    void rename_updatesName() {
        Role role = new Role("admin", "Administrator");
        role.rename("Super Admin");
        assertThat(role.getName()).isEqualTo("Super Admin");
    }

    @Test
    void setId_updatesId() {
        Role role = new Role("admin", "Administrator");
        role.setId(42L);
        assertThat(role.getId()).isEqualTo(42L);
    }
}
