package com.zhangyuan.modules.auth.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void createEnabledUser() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ENABLED);
    }

    @Test
    void disableUser() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.disable();
        assertThat(user.getStatus()).isEqualTo(UserStatus.DISABLED);
    }

    @Test
    void enableUser() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.disable();
        user.enable();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ENABLED);
    }

    @Test
    void updateProfile() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.updateProfile("New Name", "new@test.com");
        assertThat(user.getNickname()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void addRole() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.addRole("super_admin");
        assertThat(user.hasRole("super_admin")).isTrue();
    }
}
