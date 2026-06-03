package com.zhangyuan.modules.auth.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UserDomainTest {

    @Test
    void create() {
        User u = new User("admin", "hash", "Admin", "admin@test.com");
        assertThat(u.getUsername()).isEqualTo("admin");
        assertThat(u.getStatus()).isEqualTo(UserStatus.ENABLED);
    }

    @Test
    void disableAndEnable() {
        User u = new User("admin", "hash", "Admin", "admin@test.com");
        u.disable();
        assertThat(u.getStatus()).isEqualTo(UserStatus.DISABLED);
        u.enable();
        assertThat(u.getStatus()).isEqualTo(UserStatus.ENABLED);
    }

    @Test
    void updateProfile() {
        User u = new User("admin", "hash", "Admin", "admin@test.com");
        u.updateProfile("NewName", "new@test.com");
        assertThat(u.getNickname()).isEqualTo("NewName");
        assertThat(u.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void addRole() {
        User u = new User("admin", "hash", "Admin", "admin@test.com");
        u.addRole("super_admin");
        assertThat(u.hasRole("super_admin")).isTrue();
    }
}
