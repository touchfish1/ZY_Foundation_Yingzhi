package com.zhangyuan.user.adapter.in.rest;

import com.zhangyuan.user.application.service.SaasUserApplicationService;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.user.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.stp.StpUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.scheduling.annotation.Scheduled;

@RestController
@RequestMapping("/api/auth")
public class SaasUserController {
    private static final Logger log = LoggerFactory.getLogger(SaasUserController.class);
    private static final long RATE_LIMIT_PER_MILLIS = 100L;
    private final ConcurrentHashMap<String, AtomicLong> lastAccessMap = new ConcurrentHashMap<>();
    private final SaasUserApplicationService saasUserApplicationService;

    private boolean isRateLimited(String ip) {
        long now = System.currentTimeMillis();
        AtomicLong lastAccess = lastAccessMap.computeIfAbsent(ip, k -> new AtomicLong(0));
        long last = lastAccess.get();
        if (now - last < RATE_LIMIT_PER_MILLIS) {
            return true;
        }
        return !lastAccess.compareAndSet(last, now);
    }

    public SaasUserController(SaasUserApplicationService saasUserApplicationService) {
        this.saasUserApplicationService = saasUserApplicationService;
    }

    @Scheduled(fixedRate = 300_000)
    public void cleanupRateLimitMap() {
        if (lastAccessMap.size() > 10_000) {
            lastAccessMap.clear();
            log.info("Rate limit map cleared (size exceeded 10000)");
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                                HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        if (isRateLimited(ip)) {
            log.warn("Rate limit exceeded for register from IP: {}", ip);
            return ApiResponse.error(429, "请求过于频繁，请稍后重试");
        }
        log.info("User register: {}", request.email());
        return ApiResponse.ok(saasUserApplicationService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        if (isRateLimited(ip)) {
            log.warn("Rate limit exceeded for login from IP: {}", ip);
            return ApiResponse.error(429, "请求过于频繁，请稍后重试");
        }
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

    @PostMapping("/quota/{userId}")
    public ApiResponse<Void> updateQuota(@PathVariable Long userId, @RequestParam Long quotaLimit) {
        StpUtil.checkLogin();
        log.info("Admin update quota: userId={}, limit={}", userId, quotaLimit);
        saasUserApplicationService.updateQuotaLimit(userId, quotaLimit);
        return ApiResponse.ok();
    }

    @GetMapping("/verify-key")
    public ApiResponse<Long> verifyApiKey(@RequestParam String apiKey, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (isRateLimited(ip)) {
            log.warn("Rate limit exceeded for verify-key from IP: {}", ip);
            return ApiResponse.error(429, "请求过于频繁，请稍后重试");
        }
        log.info("Verifying API key");
        return saasUserApplicationService.verifyApiKey(apiKey);
    }

    @GetMapping("/verify-key-with-quota")
    public ApiResponse<UserQuotaResponse> verifyApiKeyWithQuota(@RequestParam String apiKey, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (isRateLimited(ip)) {
            return ApiResponse.error(429, "请求过于频繁，请稍后重试");
        }
        return saasUserApplicationService.verifyApiKeyWithQuota(apiKey);
    }
}
