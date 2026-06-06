package com.zhangyuan.auth.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void createUser_setsInitialState() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getNickname()).isEqualTo("Admin");
        assertThat(user.getEmail()).isEqualTo("admin@test.com");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ENABLED);
        assertThat(user.getRoleCodes()).isEmpty();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void disable_setsStatusToDisabled() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.disable();
        assertThat(user.getStatus()).isEqualTo(UserStatus.DISABLED);
    }

    @Test
    void enable_afterDisable_restoresEnabled() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.disable();
        user.enable();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ENABLED);
    }

    @Test
    void disable_isNotActive() {
        UserStatus disabled = UserStatus.DISABLED;
        assertThat(disabled.isActive()).isFalse();
    }

    @Test
    void enabled_isActive() {
        UserStatus enabled = UserStatus.ENABLED;
        assertThat(enabled.isActive()).isTrue();
    }

    @Test
    void updateProfile_changesNameAndEmail() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.updateProfile("NewName", "new@test.com");
        assertThat(user.getNickname()).isEqualTo("NewName");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void authenticate_withCorrectPassword_returnsTrue() {
        User user = new User("admin", "encoded_hash", "Admin", "admin@test.com");
        boolean result = user.authenticate("password", (raw, hash) -> "encoded_hash".equals(hash));
        assertThat(result).isTrue();
    }

    @Test
    void authenticate_withWrongPassword_returnsFalse() {
        User user = new User("admin", "encoded_hash", "Admin", "admin@test.com");
        boolean result = user.authenticate("wrong", (raw, hash) -> false);
        assertThat(result).isFalse();
    }

    @Test
    void authenticate_whenDisabled_throws() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.disable();
        assertThatThrownBy(() -> user.authenticate("pwd", (r, h) -> true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void addRole_andHasRole() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.addRole("admin");
        assertThat(user.hasRole("admin")).isTrue();
        assertThat(user.hasRole("user")).isFalse();
    }

    @Test
    void roleCodes_returnsUnmodifiableSet() {
        User user = new User("admin", "hash", "Admin", "admin@test.com");
        user.addRole("admin");
        assertThatThrownBy(() -> user.getRoleCodes().add("user"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
