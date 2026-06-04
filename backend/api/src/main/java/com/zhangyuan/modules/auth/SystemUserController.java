package com.zhangyuan.modules.auth;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.auth.domain.AdminUser;
import com.zhangyuan.modules.auth.dto.CreateUserRequest;
import com.zhangyuan.modules.auth.dto.UpdateUserRequest;
import com.zhangyuan.modules.auth.dto.UserResponse;
import com.zhangyuan.modules.auth.repository.AdminUserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台用户管理控制器，提供用户的增删改查接口。
 */
@RestController
@RequestMapping("/admin/system/users")
public class SystemUserController {

    private static final Logger log = LoggerFactory.getLogger(SystemUserController.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemUserController(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 获取所有用户列表。
     *
     * @return 用户响应列表
     */
    @GetMapping
    public ApiResponse<List<UserResponse>> listUsers() {
        log.info("Listing system users");
        return ApiResponse.ok(adminUserRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getNickname(), u.getEmail(), u.getStatus(), u.getCreatedAt()))
                .toList());
    }

    /**
     * 创建新用户。
     *
     * @param request 创建用户请求
     * @return 创建后的用户信息
     */
    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating system user: username={}", request.username());
        // 检查用户名是否已存在
        if (adminUserRepository.findByUsername(request.username()).isPresent()) {
            log.warn("Username already exists: {}", request.username());
            return ApiResponse.error(40001, "Username already exists");
        }
        // 加密密码后保存用户
        AdminUser user = new AdminUser(request.username(), passwordEncoder.encode(request.password()), request.nickname(), request.email());
        user = adminUserRepository.save(user);
        log.info("System user created: id={}, username={}", user.getId(), user.getUsername());
        return ApiResponse.ok(new UserResponse(user.getId(), user.getUsername(), user.getNickname(), user.getEmail(), user.getStatus(), user.getCreatedAt()));
    }

    /**
     * 更新用户信息。
     *
     * @param id      用户 ID
     * @param request 更新用户请求
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating system user: {}", id);
        AdminUser user = adminUserRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        // 逐个更新可修改字段
        if (request.nickname() != null) user.setNickname(request.nickname());
        if (request.email() != null) user.setEmail(request.email());
        if (request.status() != null) user.setStatus(request.status());
        user = adminUserRepository.save(user);
        log.info("System user updated: {}", id);
        return ApiResponse.ok(new UserResponse(user.getId(), user.getUsername(), user.getNickname(), user.getEmail(), user.getStatus(), user.getCreatedAt()));
    }

    /**
     * 删除指定用户。
     *
     * @param id 用户 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting system user: {}", id);
        adminUserRepository.deleteById(id);
        log.info("System user deleted: {}", id);
        return ApiResponse.ok();
    }
}
