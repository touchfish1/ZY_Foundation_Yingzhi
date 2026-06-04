package com.zhangyuan.auth.adapter.in.rest;

import com.zhangyuan.auth.common.ApiResponse;
import com.zhangyuan.auth.domain.model.Role;
import com.zhangyuan.auth.domain.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DDD 版角色管理控制器，提供角色的查询和创建接口。
 */
@RestController
@RequestMapping("/api/ddd/auth/roles")
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * 获取所有角色列表。
     *
     * @return 角色列表
     */
    @GetMapping
    public ApiResponse<List<Role>> listRoles() {
        log.info("Listing all roles");
        return ApiResponse.ok(roleRepository.findAll());
    }

    /**
     * 创建新角色。
     *
     * @param request 创建角色请求
     * @return 创建后的角色
     */
    @PostMapping
    public ApiResponse<Role> createRole(@RequestBody CreateRoleRequest request) {
        log.info("Creating role: code={}, name={}", request.code(), request.name());
        Role role = new Role(request.code(), request.name());
        Role saved = roleRepository.save(role);
        log.info("Role created: id={}, code={}", saved.getId(), saved.getCode());
        return ApiResponse.ok(saved);
    }

    public record CreateRoleRequest(String code, String name) {}
}
