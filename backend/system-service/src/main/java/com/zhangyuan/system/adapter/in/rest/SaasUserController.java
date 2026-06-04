package com.zhangyuan.system.adapter.in.rest;

import com.zhangyuan.system.application.service.SaasUserApplicationService;
import com.zhangyuan.system.common.ApiResponse;
import com.zhangyuan.system.dto.*;
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
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("User register: {}", request.email());
        return saasUserApplicationService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("User login: {}", request.email());
        return saasUserApplicationService.login(request);
    }

    @GetMapping("/profile")
    public UserResponse profile() {
        long loginId = StpUtil.getLoginIdAsLong();
        return saasUserApplicationService.getProfile(loginId);
    }

    @GetMapping("/verify-key")
    public ApiResponse<Long> verifyApiKey(@RequestParam String apiKey) {
        log.info("Verifying API key");
        return saasUserApplicationService.verifyApiKey(apiKey);
    }
}
