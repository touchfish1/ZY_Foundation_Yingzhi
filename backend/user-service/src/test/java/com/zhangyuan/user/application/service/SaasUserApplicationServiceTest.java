package com.zhangyuan.user.application.service;

import com.zhangyuan.user.domain.model.User;
import com.zhangyuan.user.domain.repository.UserRepository;
import com.zhangyuan.user.dto.LoginRequest;
import com.zhangyuan.user.dto.LoginResponse;
import com.zhangyuan.user.dto.RegisterRequest;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SaasUserApplicationServiceTest {

    private final UserRepository userRepo = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private SaasUserApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SaasUserApplicationService(userRepo, passwordEncoder);
    }

    @Test
    void register_createsUser() {
        try (var stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.login(any())).thenAnswer(i -> null);
            stpUtil.when(StpUtil::getTokenValue).thenReturn("mock-token");

            when(userRepo.existsByEmail("new@test.com")).thenReturn(false);
            when(userRepo.save(any())).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setId(1L);
                return u;
            });

            LoginResponse resp = service.register(new RegisterRequest("new@test.com", "pass123", "NewUser"));

            assertThat(resp.user().email()).isEqualTo("new@test.com");
            assertThat(resp.token()).isEqualTo("mock-token");
            assertThat(resp.user().apiKey()).startsWith("sk-");
        }
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepo.existsByEmail("dup@test.com")).thenReturn(true);
        assertThatThrownBy(() -> service.register(new RegisterRequest("dup@test.com", "pass", "Dup")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_validCredentials_success() {
        try (var stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.login(any())).thenAnswer(i -> null);
            stpUtil.when(StpUtil::getTokenValue).thenReturn("mock-token");

            String rawPassword = "pass123";
            String encoded = passwordEncoder.encode(rawPassword);
            User user = new User("test@test.com", encoded, "Test");
            user.setId(1L);
            when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));

            LoginResponse resp = service.login(new LoginRequest("test@test.com", rawPassword));
            assertThat(resp.user().email()).isEqualTo("test@test.com");
            assertThat(resp.token()).isEqualTo("mock-token");
        }
    }

    @Test
    void login_wrongPassword_throws() {
        User user = new User("test@test.com", passwordEncoder.encode("correct"), "Test");
        user.setId(1L);
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest("test@test.com", "wrong")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void login_emailNotFound_throws() {
        when(userRepo.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login(new LoginRequest("nonexistent@test.com", "pass")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void verifyApiKey_validKey_returnsUserId() {
        User user = new User("test@test.com", "hash", "Test");
        user.setId(1L);
        user.setApiKey("sk-valid-key");
        when(userRepo.findByApiKey("sk-valid-key")).thenReturn(Optional.of(user));

        var resp = service.verifyApiKey("sk-valid-key");
        assertThat(resp.code()).isZero();
        assertThat(resp.data()).isEqualTo(1L);
    }

    @Test
    void verifyApiKey_invalidKey_returnsError() {
        when(userRepo.findByApiKey("sk-invalid")).thenReturn(Optional.empty());

        var resp = service.verifyApiKey("sk-invalid");
        assertThat(resp.code()).isEqualTo(404);
        assertThat(resp.message()).contains("not found");
    }

    @Test
    void verifyApiKey_disabledUser_returnsError() {
        User user = new User("test@test.com", "hash", "Test");
        user.setStatus("disabled");
        when(userRepo.findByApiKey("sk-disabled")).thenReturn(Optional.of(user));

        var resp = service.verifyApiKey("sk-disabled");
        assertThat(resp.code()).isEqualTo(403);
        assertThat(resp.message()).contains("not active");
    }
}
