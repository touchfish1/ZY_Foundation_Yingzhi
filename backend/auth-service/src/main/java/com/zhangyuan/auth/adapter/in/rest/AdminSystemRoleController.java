package com.zhangyuan.auth.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.auth.common.ApiResponse;
import com.zhangyuan.auth.domain.model.Role;
import com.zhangyuan.auth.domain.repository.RoleRepository;
import com.zhangyuan.auth.dto.CreateRoleRequest;
import com.zhangyuan.auth.dto.RoleResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public AdminSystemRoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> listRoles() {
        log.info("Listing system roles");
        return ApiResponse.ok(roleRepository.findAll().stream()
                .map(r -> new RoleResponse(r.getId(), r.getCode(), r.getName(), r.getCreatedAt()))
                .toList());
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

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        log.info("Deleting system role: {}", id);
        roleRepository.deleteById(id);
        log.info("System role deleted: {}", id);
        return ApiResponse.ok();
    }
}
