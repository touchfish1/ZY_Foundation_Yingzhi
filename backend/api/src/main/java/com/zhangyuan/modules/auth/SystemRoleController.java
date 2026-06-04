package com.zhangyuan.modules.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.auth.domain.AdminRole;
import com.zhangyuan.modules.auth.dto.CreateRoleRequest;
import com.zhangyuan.modules.auth.dto.RoleResponse;
import com.zhangyuan.modules.auth.repository.AdminRoleRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台角色管理控制器，提供角色的查询和创建接口。
 */
@RestController
@RequestMapping("/admin/system/roles")
@SaCheckPermission("system:role:list")
public class SystemRoleController {

    private static final Logger log = LoggerFactory.getLogger(SystemRoleController.class);

    private final AdminRoleRepository adminRoleRepository;

    public SystemRoleController(AdminRoleRepository adminRoleRepository) {
        this.adminRoleRepository = adminRoleRepository;
    }

    /**
     * 获取所有角色列表。
     *
     * @return 角色响应列表
     */
    @GetMapping
    public ApiResponse<List<RoleResponse>> listRoles() {
        log.info("Listing system roles");
        return ApiResponse.ok(adminRoleRepository.findAll().stream()
                .map(r -> new RoleResponse(r.getId(), r.getCode(), r.getName(), r.getCreatedAt()))
                .toList());
    }

    /**
     * 创建新角色。
     *
     * @param request 创建角色请求
     * @return 创建后的角色信息
     */
    @PostMapping
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Creating system role: code={}, name={}", request.code(), request.name());
        AdminRole role = new AdminRole(request.code(), request.name());
        role = adminRoleRepository.save(role);
        log.info("System role created: id={}, code={}", role.getId(), role.getCode());
        return ApiResponse.ok(new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getCreatedAt()));
    }
}
