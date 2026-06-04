package com.zhangyuan.modules.auth;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.auth.domain.AdminUser;
import com.zhangyuan.modules.auth.dto.CreateUserRequest;
import com.zhangyuan.modules.auth.dto.UpdateUserRequest;
import com.zhangyuan.modules.auth.dto.UserResponse;
import com.zhangyuan.modules.auth.repository.AdminUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemUserControllerTest {

    private final AdminUserRepository adminUserRepository = mock(AdminUserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final SystemUserController controller = new SystemUserController(adminUserRepository, passwordEncoder);

    @Test
    void listUsersReturnsAll() {
        when(adminUserRepository.findAll()).thenReturn(List.of(
                new AdminUser("admin", "hash", "Admin", "admin@test.com")
        ));

        ApiResponse<List<UserResponse>> response = controller.listUsers();

        assertThat(response.code()).isZero();
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().username()).isEqualTo("admin");
    }

    @Test
    void createUserSavesAndReturns() {
        CreateUserRequest request = new CreateUserRequest("newuser", "pass123", "New User", "new@test.com");
        when(adminUserRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encoded-hash");
        when(adminUserRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<UserResponse> response = controller.createUser(request);

        assertThat(response.code()).isZero();
        assertThat(response.data().username()).isEqualTo("newuser");
        verify(adminUserRepository).save(any());
    }

    @Test
    void createUserWithDuplicateUsernameReturnsError() {
        CreateUserRequest request = new CreateUserRequest("admin", "pass", null, null);
        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(mock(AdminUser.class)));

        ApiResponse<UserResponse> response = controller.createUser(request);

        assertThat(response.code()).isEqualTo(40001);
    }
}
