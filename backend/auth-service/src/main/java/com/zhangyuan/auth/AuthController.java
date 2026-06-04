package com.zhangyuan.auth;

import com.zhangyuan.auth.common.ApiResponse;
import com.zhangyuan.auth.dto.LoginRequest;
import com.zhangyuan.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台认证控制器，提供登录和获取当前用户信息接口。
 */
@RestController
@RequestMapping("/admin/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录，验证用户名密码并返回 JWT 令牌。
     *
     * @param request 登录请求（用户名 + 密码）
     * @return 登录响应（含令牌和用户信息）
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt: username={}", request.username());
        return ApiResponse.ok(authService.login(request.username(), request.password()));
    }

    /**
     * 获取当前已登录用户的信息。
     *
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me() {
        log.info("Getting current user info");
        return ApiResponse.ok(authService.currentUser());
    }
}
