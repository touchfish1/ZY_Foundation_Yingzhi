package com.zhangyuan.auth.adapter.in.rest;

import com.zhangyuan.auth.application.service.AuthApplicationService;
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
public class AdminAuthController {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthController.class);

    private final AuthApplicationService authApplicationService;

    public AdminAuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt: username={}", request.username());
        return ApiResponse.ok(authApplicationService.login(request.username(), request.password()));
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me() {
        log.info("Getting current user info");
        return ApiResponse.ok(authApplicationService.currentUser());
    }
}
