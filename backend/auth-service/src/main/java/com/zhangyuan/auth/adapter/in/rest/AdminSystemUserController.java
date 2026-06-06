package com.zhangyuan.auth.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.auth.application.service.AuthApplicationService;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.auth.repository.AdminUserRepository;
import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.auth.domain.model.User;
import com.zhangyuan.auth.dto.CreateUserRequest;
import com.zhangyuan.auth.dto.SetRoleRequest;
import com.zhangyuan.auth.dto.UpdateUserRequest;
import com.zhangyuan.auth.dto.UserResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台用户管理控制器，提供用户的增删改查接口。
 */
@RestController
@RequestMapping("/admin/system/users")
@SaCheckPermission("system:user:list")
public class AdminSystemUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminSystemUserController.class);

    private final AuthApplicationService authApplicationService;
    @SuppressWarnings("unused")
    private final AdminUserRepository adminUserRepository;

    public AdminSystemUserController(AuthApplicationService authApplicationService,
                                     AdminUserRepository adminUserRepository) {
        this.authApplicationService = authApplicationService;
        this.adminUserRepository = adminUserRepository;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("Listing system users, keyword={}, page={}, pageSize={}", keyword, page, pageSize);
        return ApiResponse.ok(authApplicationService.listUsersPaginated(keyword, page, pageSize));
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating system user: username={}", request.username());
        try {
            User user = authApplicationService.createUser(request.username(), request.password(),
                    request.nickname(), request.email());
            log.info("System user created: id={}, username={}", user.getId(), user.getUsername());
            return ApiResponse.ok(new UserResponse(user.getId(), user.getUsername(), user.getNickname(),
                    user.getEmail(), user.getStatus().name().toLowerCase(), user.getCreatedAt()));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create user: {}", e.getMessage());
            return ApiResponse.error(40001, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating system user: {}", id);
        try {
            User user = authApplicationService.updateUser(id, request.nickname(), request.email(), request.status());
            log.info("System user updated: {}", id);
            return ApiResponse.ok(new UserResponse(user.getId(), user.getUsername(), user.getNickname(),
                    user.getEmail(), user.getStatus().name().toLowerCase(), user.getCreatedAt()));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update user: {}", e.getMessage());
            return ApiResponse.error(404, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting system user: {}", id);
        authApplicationService.deleteUser(id);
        log.info("System user deleted: {}", id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/roles")
    public ApiResponse<List<Long>> getUserRoles(@PathVariable Long id) {
        return ApiResponse.ok(authApplicationService.getUserRoleIds(id));
    }

    @PutMapping("/{id}/roles")
    @SaCheckPermission("system:user:update")
    public ApiResponse<Void> setUserRoles(@PathVariable Long id,
                                          @Valid @RequestBody SetRoleRequest request) {
        authApplicationService.setUserRoles(id, request.roleIds());
        return ApiResponse.ok();
    }
}
