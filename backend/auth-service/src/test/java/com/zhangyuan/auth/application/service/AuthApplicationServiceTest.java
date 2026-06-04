package com.zhangyuan.auth.application.service;

import com.zhangyuan.auth.adapter.out.persistence.AdminUser;
import com.zhangyuan.auth.common.PageResponse;
import com.zhangyuan.auth.domain.repository.UserRepository;
import com.zhangyuan.auth.domain.service.AuthDomainService;
import com.zhangyuan.auth.dto.UserResponse;
import com.zhangyuan.auth.repository.AdminRoleRepository;
import com.zhangyuan.auth.repository.AdminUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.auth.common.security.AuthUser;
import com.zhangyuan.auth.dto.LoginResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthApplicationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthDomainService authDomainService = mock(AuthDomainService.class);
    private final com.zhangyuan.auth.common.security.AuthUserService authUserService =
            mock(com.zhangyuan.auth.common.security.AuthUserService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AdminUserRepository adminUserRepository = mock(AdminUserRepository.class);
    private final AdminRoleRepository adminRoleRepository = mock(AdminRoleRepository.class);
    private AuthApplicationService service;

    @BeforeEach
    void setUp() {
        service = new AuthApplicationService(
                userRepository, authDomainService, authUserService, passwordEncoder,
                adminUserRepository, adminRoleRepository);
    }

    private static AdminUser createUser(Long id, String username, String nickname) {
        AdminUser user = new AdminUser(username, "password-hash", nickname);
        setId(user, id);
        return user;
    }

    private static void setId(AdminUser user, Long id) {
        try {
            Field idField = AdminUser.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id on AdminUser", e);
        }
    }

    @Test
    void listUsersPaginatedWithoutKeyword() {
        var users = List.of(
                createUser(1L, "admin", "Super Admin"),
                createUser(2L, "editor", "Editor"));
        var pageable = PageRequest.of(0, 20);
        // total must be >= offset + pageSize (0+20=20) to avoid PageImpl adjusting it
        Page<AdminUser> page = new PageImpl<>(users, pageable, 25);
        when(adminUserRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageResponse<UserResponse> result = service.listUsersPaginated(null, 1, 20);

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(20);
        assertThat(result.total()).isEqualTo(25);
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).id()).isEqualTo(1L);
        assertThat(result.items().get(0).username()).isEqualTo("admin");
        assertThat(result.items().get(0).nickname()).isEqualTo("Super Admin");
        assertThat(result.items().get(1).id()).isEqualTo(2L);
        assertThat(result.items().get(1).username()).isEqualTo("editor");
        verify(adminUserRepository).findAll(pageable);
        verifyNoMoreInteractions(adminUserRepository);
    }

    @Test
    void listUsersPaginatedWithKeyword() {
        var matched = List.of(createUser(1L, "admin", "Super Admin"));
        var pageable = PageRequest.of(0, 10);
        Page<AdminUser> page = new PageImpl<>(matched, pageable, 1);
        when(adminUserRepository.searchByKeyword(eq("admin"), any(PageRequest.class)))
                .thenReturn(page);

        PageResponse<UserResponse> result = service.listUsersPaginated("admin", 1, 10);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).username()).isEqualTo("admin");
        assertThat(result.items().get(0).nickname()).isEqualTo("Super Admin");
        verify(adminUserRepository).searchByKeyword(eq("admin"), eq(pageable));
        verifyNoMoreInteractions(adminUserRepository);
    }

    @Test
    void listUsersPaginatedWithBlankKeywordFallsBackToFindAll() {
        var users = List.of(createUser(1L, "user1", "User One"));
        var pageable = PageRequest.of(2, 15);
        // total must be >= offset + pageSize (30+15=45) to avoid PageImpl adjusting it
        Page<AdminUser> page = new PageImpl<>(users, pageable, 50);
        when(adminUserRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageResponse<UserResponse> result = service.listUsersPaginated("   ", 3, 15);

        assertThat(result.page()).isEqualTo(3);
        assertThat(result.total()).isEqualTo(50);
        verify(adminUserRepository).findAll(pageable);
        verify(adminUserRepository, never()).searchByKeyword(any(), any());
    }

    @Test
    void listUsersPaginatedWithNoResults() {
        var pageable = PageRequest.of(0, 20);
        Page<AdminUser> page = new PageImpl<>(List.of(), pageable, 0);
        when(adminUserRepository.searchByKeyword(eq("nonexistent"), any(PageRequest.class)))
                .thenReturn(page);

        PageResponse<UserResponse> result = service.listUsersPaginated("nonexistent", 1, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
        assertThat(result.page()).isEqualTo(1);
    }

    @Test
    void listUsersPaginatedPageCalculation() {
        var user = createUser(1L, "test", "Test");
        var pageable = PageRequest.of(1, 10); // page 2 internally (0-based)
        Page<AdminUser> page = new PageImpl<>(List.of(user), pageable, 3);
        when(adminUserRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageResponse<UserResponse> result = service.listUsersPaginated(null, 2, 10);

        // PageResponse.from converts 0-based back to 1-based, so it should be 2
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.pageSize()).isEqualTo(10);
    }

    @Test
    void login_validCredentials_returnsLoginResponse() {
        var authUser = new AuthUser(1L, "admin", "encodedPassword", "Admin",
                List.of("cms:read", "cms:write"), true);
        when(authUserService.loadUserByUsername("admin")).thenReturn(authUser);
        when(passwordEncoder.matches("admin123", "encodedPassword")).thenReturn(true);
        when(authUserService.findRoleCodes("admin")).thenReturn(List.of("admin", "editor"));

        try (var stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(() -> StpUtil.login(1L)).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn("mock-token-value");
            stpUtilMock.when(StpUtil::getTokenTimeout).thenReturn(7200L);

            var session = mock(SaSession.class);
            stpUtilMock.when(StpUtil::getSession).thenReturn(session);

            var result = service.login("admin", "admin123");

            assertThat(result.accessToken()).isEqualTo("mock-token-value");
            assertThat(result.expiresIn()).isEqualTo(7200L);
            assertThat(result.user().id()).isEqualTo(1L);
            assertThat(result.user().username()).isEqualTo("admin");
            assertThat(result.user().nickname()).isEqualTo("Admin");
            assertThat(result.permissions()).containsExactly("cms:read", "cms:write");

            verify(session).set("username", "admin");
            verify(session).set("nickname", "Admin");
            verify(session).set(SaSession.PERMISSION_LIST, List.of("cms:read", "cms:write"));
            verify(session).set(SaSession.ROLE_LIST, List.of("admin", "editor"));
        }
    }

    @Test
    void login_invalidPassword_throwsBadCredentialsException() {
        var authUser = new AuthUser(1L, "admin", "encodedPassword", "Admin", List.of(), true);
        when(authUserService.loadUserByUsername("admin")).thenReturn(authUser);
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> service.login("admin", "wrong"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void login_disabledUser_throwsBadCredentialsException() {
        var authUser = new AuthUser(1L, "disabled", "encodedPassword", "Disabled", List.of(), false);
        when(authUserService.loadUserByUsername("disabled")).thenReturn(authUser);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> service.login("disabled", "password"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void listAll_returnsUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
            new com.zhangyuan.auth.domain.model.User("user1", "hash", "nick1", "user1@test.com"),
            new com.zhangyuan.auth.domain.model.User("user2", "hash", "nick2", "user2@test.com")
        ));
        var result = service.listAll();
        assertThat(result).hasSize(2);
    }
}
