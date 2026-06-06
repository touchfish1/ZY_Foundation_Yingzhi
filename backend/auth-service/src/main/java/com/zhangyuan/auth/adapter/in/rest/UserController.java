package com.zhangyuan.auth.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.auth.application.service.AuthApplicationService;
import com.zhangyuan.auth.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DDD 版用户管理控制器，提供用户的增删改查接口。
 */
@RestController
@RequestMapping("/api/ddd/auth")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final AuthApplicationService authApplicationService;

    public UserController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    /**
     * 获取所有用户列表。
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    public ApiResponse<List<User>> listUsers() {
        log.info("Listing all users");
        return ApiResponse.ok(authApplicationService.listAll());
    }

    /**
     * 创建新用户。
     *
     * @param request 创建用户请求
     * @return 创建后的用户
     */
    @PostMapping("/users")
    public ApiResponse<User> createUser(@RequestBody CreateUserRequest request) {
        log.info("Creating user: username={}", request.username());
        User user = authApplicationService.createUser(
                request.username(), request.passwordHash(), request.nickname(), request.email());
        log.info("User created: id={}, username={}", user.getId(), user.getUsername());
        return ApiResponse.ok(user);
    }

    /**
     * 根据 ID 查询用户。
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/users/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        log.info("Getting user by id: {}", id);
        return authApplicationService.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error(404, "User not found"));
    }

    /**
     * 禁用指定用户。
     *
     * @param id 用户 ID
     * @return 成功响应
     */
    @PutMapping("/users/{id}/disable")
    public ApiResponse<Void> disableUser(@PathVariable Long id) {
        log.info("Disabling user: {}", id);
        authApplicationService.disableUser(id);
        log.info("User disabled: {}", id);
        return ApiResponse.ok();
    }

    public record CreateUserRequest(String username, String passwordHash, String nickname, String email) {}
}
