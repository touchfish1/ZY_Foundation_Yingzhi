package com.zhangyuan.auth.adapter.in.rest;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.auth.application.service.MenuApplicationService;
import com.zhangyuan.auth.application.service.AuthApplicationService;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.auth.dto.LoginRequest;
import com.zhangyuan.auth.dto.LoginResponse;
import com.zhangyuan.auth.dto.MenuResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 后台认证控制器，提供登录、获取当前用户信息和获取当前用户菜单接口。
 */
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthController.class);

    private final AuthApplicationService authApplicationService;
    private final MenuApplicationService menuApplicationService;

    public AdminAuthController(AuthApplicationService authApplicationService,
                                MenuApplicationService menuApplicationService) {
        this.authApplicationService = authApplicationService;
        this.menuApplicationService = menuApplicationService;
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

    @GetMapping("/menus")
    public ApiResponse<List<MenuResponse>> menus() {
        log.info("Getting current user's menu tree");

        // Get current user's permissions from SaSession
        List<String> rawPermissions = (List<String>) StpUtil.getSession()
                .get(SaSession.PERMISSION_LIST);
        List<String> userPermissions = rawPermissions != null ? rawPermissions : Collections.emptyList();

        return ApiResponse.ok(menuApplicationService.getCurrentUserMenus(userPermissions));
    }
}
