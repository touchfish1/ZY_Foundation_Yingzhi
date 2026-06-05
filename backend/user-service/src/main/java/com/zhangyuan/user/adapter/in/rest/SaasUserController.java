package com.zhangyuan.user.adapter.in.rest;

import com.zhangyuan.user.application.service.SaasUserApplicationService;
import com.zhangyuan.user.common.ApiResponse;
import com.zhangyuan.user.dto.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/api/auth")
public class SaasUserController {
    private static final Logger log = LoggerFactory.getLogger(SaasUserController.class);
    private final SaasUserApplicationService saasUserApplicationService;

    public SaasUserController(SaasUserApplicationService saasUserApplicationService) {
        this.saasUserApplicationService = saasUserApplicationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("User register: {}", request.email());
        return ApiResponse.ok(saasUserApplicationService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login: {}", request.email());
        return ApiResponse.ok(saasUserApplicationService.login(request));
    }

    @GetMapping("/profile")
    public ApiResponse<UserResponse> profile() {
        long loginId = StpUtil.getLoginIdAsLong();
        return ApiResponse.ok(saasUserApplicationService.getProfile(loginId));
    }

    @PutMapping("/keys/regenerate")
    public ApiResponse<UserResponse> regenerateApiKey() {
        long loginId = StpUtil.getLoginIdAsLong();
        return ApiResponse.ok(saasUserApplicationService.regenerateApiKey(loginId));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        long loginId = StpUtil.getLoginIdAsLong();
        saasUserApplicationService.changePassword(loginId, request);
        return ApiResponse.ok();
    }

    @GetMapping("/verify-key")
    public ApiResponse<Long> verifyApiKey(@RequestParam String apiKey) {
        log.info("Verifying API key");
        return saasUserApplicationService.verifyApiKey(apiKey);
    }
}
