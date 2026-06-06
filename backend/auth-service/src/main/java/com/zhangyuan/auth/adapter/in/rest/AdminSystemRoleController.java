package com.zhangyuan.auth.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.auth.application.service.PermissionApplicationService;
import com.zhangyuan.auth.common.ApiResponse;
import com.zhangyuan.auth.common.PageResponse;
import com.zhangyuan.auth.domain.model.Role;
import com.zhangyuan.auth.domain.repository.RoleRepository;
import com.zhangyuan.auth.dto.CreateRoleRequest;
import com.zhangyuan.auth.dto.RoleResponse;
import com.zhangyuan.auth.dto.SetPermissionRequest;
import com.zhangyuan.auth.repository.AdminRoleRepository;
import com.zhangyuan.auth.repository.AdminUserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台角色管理控制器，提供角色的增删改查接口。
 */
@RestController
@RequestMapping("/admin/system/roles")
@SaCheckPermission("system:role:list")
public class AdminSystemRoleController {

    private static final Logger log = LoggerFactory.getLogger(AdminSystemRoleController.class);

    private final RoleRepository roleRepository;
    private final PermissionApplicationService permissionApplicationService;
    private final AdminRoleRepository adminRoleRepository;
    private final AdminUserRepository adminUserRepository;

    @GetMapping
    public ApiResponse<PageResponse<RoleResponse>> listRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("Listing system roles, page={}, pageSize={}", page, pageSize);
        var pageable = PageRequest.of(page - 1, pageSize);
        var pageResult = adminRoleRepository.findAll(pageable);
        List<RoleResponse> items = pageResult.getContent().stream()
                .map(r -> new RoleResponse(r.getId(), r.getCode(), r.getName(), r.getCreatedAt()))
                .toList();
        return ApiResponse.ok(PageResponse.from(pageResult, items));
    }

    @PostMapping
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Creating system role: code={}, name={}", request.code(), request.name());
        Role role = new Role(request.code(), request.name());
        role = roleRepository.save(role);
        log.info("System role created: id={}, code={}", role.getId(), role.getCode());
        return ApiResponse.ok(new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getCreatedAt()));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody CreateRoleRequest request) {
        log.info("Updating system role: id={}", id);
        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        existing.rename(request.name());
        existing = roleRepository.save(existing);
        log.info("System role updated: {}", id);
        return ApiResponse.ok(new RoleResponse(existing.getId(), existing.getCode(), existing.getName(), existing.getCreatedAt()));
    }

    private final AdminUserRepository adminUserRepository;

    public AdminSystemRoleController(RoleRepository roleRepository,
                                     PermissionApplicationService permissionApplicationService,
                                     AdminRoleRepository adminRoleRepository,
                                     AdminUserRepository adminUserRepository) {
        this.roleRepository = roleRepository;
        this.permissionApplicationService = permissionApplicationService;
        this.adminRoleRepository = adminRoleRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        log.info("Deleting system role: {}", id);
        var role = adminRoleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + id));

        // 级联清理用户-角色关联
        var usersWithRole = adminUserRepository.findAll();
        for (var user : usersWithRole) {
            if (user.getRoles().remove(role)) {
                adminUserRepository.save(user);
            }
        }

        // 清理角色-权限关联
        role.getPermissions().clear();
        adminRoleRepository.save(role);

        roleRepository.deleteById(id);
        log.info("System role deleted: {}", id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/permissions")
    public ApiResponse<List<Long>> getRolePermissions(@PathVariable Long id) {
        return ApiResponse.ok(permissionApplicationService.getRolePermissionIds(id));
    }

    @PutMapping("/{id}/permissions")
    @SaCheckPermission("system:role:update")
    public ApiResponse<Void> setRolePermissions(@PathVariable Long id,
                                                @Valid @RequestBody SetPermissionRequest request) {
        permissionApplicationService.setRolePermissions(id, request.permissionIds());
        return ApiResponse.ok();
    }
}
