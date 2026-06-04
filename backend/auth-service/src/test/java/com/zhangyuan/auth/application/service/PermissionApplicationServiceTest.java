package com.zhangyuan.auth.application.service;

import com.zhangyuan.auth.adapter.out.persistence.AdminPermission;
import com.zhangyuan.auth.domain.repository.PermissionRepository;
import com.zhangyuan.auth.repository.AdminPermissionRepository;
import com.zhangyuan.auth.repository.AdminRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionApplicationServiceTest {

    private final AdminPermissionRepository adminPermissionRepository = mock(AdminPermissionRepository.class);
    private final PermissionRepository permissionRepository = mock(PermissionRepository.class);
    private final AdminRoleRepository adminRoleRepository = mock(AdminRoleRepository.class);
    private PermissionApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PermissionApplicationService(permissionRepository, adminRoleRepository, adminPermissionRepository);
    }

    @SuppressWarnings("unchecked")
    private <T> Page<T> mockPage(List<T> content, int pageNumber, int pageSize, long totalElements) {
        Page<T> page = mock(Page.class);
        when(page.getContent()).thenReturn(content);
        when(page.getNumber()).thenReturn(pageNumber);
        when(page.getSize()).thenReturn(pageSize);
        when(page.getTotalElements()).thenReturn(totalElements);
        return page;
    }

    @Test
    void listPermissionsPaginatedReturnsCorrectPage() {
        var perms = List.of(
            new AdminPermission("cms:page:read", "查看页面", "cms"),
            new AdminPermission("asset:list", "文件列表", "asset")
        );
        Page<AdminPermission> page = mockPage(perms, 0, 20, 10);
        when(adminPermissionRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        var result = service.listPermissionsPaginated(null, null, 1, 20);

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(20);
        assertThat(result.total()).isEqualTo(10);
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).code()).isEqualTo("cms:page:read");
    }

    @Test
    void listPermissionsPaginatedWithModuleFilter() {
        var systemPerms = List.of(
            new AdminPermission("system:user:list", "用户列表", "system")
        );
        Page<AdminPermission> page = mockPage(systemPerms, 0, 20, 1);
        when(adminPermissionRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        var result = service.listPermissionsPaginated(List.of("system"), null, 1, 20);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.items().get(0).code()).isEqualTo("system:user:list");
    }

    @Test
    void listPermissionsPaginatedWithKeywordSearch() {
        var matched = List.of(
            new AdminPermission("cms:page:create", "新建页面", "cms")
        );
        Page<AdminPermission> page = mockPage(matched, 0, 5, 1);
        when(adminPermissionRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        var result = service.listPermissionsPaginated(null, "create", 1, 5);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.items().get(0).code()).contains("create");
    }

    @Test
    void listPermissionsPaginatedWithNoResults() {
        Page<AdminPermission> page = mockPage(List.of(), 0, 20, 0);
        when(adminPermissionRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        var result = service.listPermissionsPaginated(null, "nonexistent", 1, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
    }

    @Test
    void listPermissionsPaginated_withEmptyModuleList_returnsAll() {
        var allPerms = List.of(
            new AdminPermission("cms:read", "读取CMS", "cms"),
            new AdminPermission("sys:read", "读取系统", "system")
        );
        var pageable = PageRequest.of(0, 20);
        Page<AdminPermission> page = new PageImpl<>(allPerms, pageable, 2);
        when(adminPermissionRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(page);

        var result = service.listPermissionsPaginated(List.of(), null, 1, 20);

        assertThat(result.total()).isEqualTo(2);
    }

    @Test
    void listPermissionsPaginated_withLargePage_returnsEmpty() {
        var pageable = PageRequest.of(100, 20);
        Page<AdminPermission> page = new PageImpl<>(List.of(), pageable, 28);
        when(adminPermissionRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(page);

        var result = service.listPermissionsPaginated(null, null, 101, 20);

        assertThat(result.items()).isEmpty();
    }

    @Test
    void listPermissionsPaginated_withAllFiltersApplied() {
        // Combined module filter + keyword search
        var matched = List.of(
            new AdminPermission("system:user:list", "用户列表", "system")
        );
        var pageable = PageRequest.of(0, 20);
        Page<AdminPermission> page = new PageImpl<>(matched, pageable, 1);
        when(adminPermissionRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(page);

        var result = service.listPermissionsPaginated(List.of("system"), "user", 1, 20);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.items().get(0).code()).contains("user");
    }
}
