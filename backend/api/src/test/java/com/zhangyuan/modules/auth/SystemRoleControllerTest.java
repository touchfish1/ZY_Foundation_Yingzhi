package com.zhangyuan.modules.auth;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.auth.domain.AdminRole;
import com.zhangyuan.modules.auth.dto.CreateRoleRequest;
import com.zhangyuan.modules.auth.dto.RoleResponse;
import com.zhangyuan.modules.auth.repository.AdminRoleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemRoleControllerTest {

    private final AdminRoleRepository adminRoleRepository = mock(AdminRoleRepository.class);
    private final SystemRoleController controller = new SystemRoleController(adminRoleRepository);

    @Test
    void listRolesReturnsAll() {
        when(adminRoleRepository.findAll()).thenReturn(List.of(
                new AdminRole("super_admin", "Super Admin")
        ));

        ApiResponse<List<RoleResponse>> response = controller.listRoles();

        assertThat(response.code()).isZero();
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().code()).isEqualTo("super_admin");
    }

    @Test
    void createRoleSavesAndReturns() {
        CreateRoleRequest request = new CreateRoleRequest("editor", "Editor");
        when(adminRoleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<RoleResponse> response = controller.createRole(request);

        assertThat(response.code()).isZero();
        assertThat(response.data().code()).isEqualTo("editor");
        verify(adminRoleRepository).save(any());
    }
}
