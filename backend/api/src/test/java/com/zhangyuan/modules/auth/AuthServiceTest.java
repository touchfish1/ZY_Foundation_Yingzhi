package com.zhangyuan.modules.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.common.security.AuthUser;
import com.zhangyuan.common.security.AuthUserService;
import com.zhangyuan.modules.auth.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final AuthUserService authUserService = mock(AuthUserService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthService authService = new AuthService(authUserService, passwordEncoder);

    @Test
    void loginSuccessful() {
        AuthUser user = new AuthUser(1L, "admin", "hash", "Admin", List.of("cms:page:read"), true);
        when(authUserService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("admin123", "hash")).thenReturn(true);
        when(authUserService.findRoleCodes("admin")).thenReturn(List.of("super_admin"));

        try (MockedStatic<StpUtil> mockedStp = mockStatic(StpUtil.class)) {
            mockedStp.when(StpUtil::getTokenValue).thenReturn("sa-token-uuid");
            mockedStp.when(StpUtil::getTokenTimeout).thenReturn(7200L);

            LoginResponse response = authService.login("admin", "admin123");

            assertThat(response.accessToken()).isEqualTo("sa-token-uuid");
            assertThat(response.expiresIn()).isEqualTo(7200L);
            assertThat(response.user()).isNotNull();
            assertThat(response.user().username()).isEqualTo("admin");
        }
    }

    @Test
    void loginWithWrongPasswordThrows() {
        AuthUser user = new AuthUser(1L, "admin", "hash", "Admin", List.of(), true);
        when(authUserService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("admin", "wrong"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void loginWithDisabledUserThrows() {
        AuthUser user = new AuthUser(2L, "disabled-user", "hash", "Disabled", List.of(), false);
        when(authUserService.loadUserByUsername("disabled-user")).thenReturn(user);
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login("disabled-user", "pass"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Admin user is disabled");
    }

    @Test
    void currentUserReturnsUserWithoutToken() {
        AuthUser authUser = new AuthUser(1L, "admin", "hash", "Admin", List.of("cms:page:read"), true);
        when(authUserService.loadUserById(1L)).thenReturn(authUser);
        when(authUserService.findRoleCodes("admin")).thenReturn(List.of("super_admin"));

        try (MockedStatic<StpUtil> mockedStp = mockStatic(StpUtil.class)) {
            mockedStp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            LoginResponse response = authService.currentUser();

            assertThat(response.accessToken()).isNull();
            assertThat(response.user()).isNotNull();
            assertThat(response.user().username()).isEqualTo("admin");
            assertThat(response.permissions()).contains("cms:page:read");
        }
    }
}
