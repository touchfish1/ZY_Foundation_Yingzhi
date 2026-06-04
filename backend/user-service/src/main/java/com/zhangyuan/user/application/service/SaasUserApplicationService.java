package com.zhangyuan.user.application.service;

import com.zhangyuan.user.common.ApiResponse;
import com.zhangyuan.user.domain.model.User;
import com.zhangyuan.user.domain.repository.UserRepository;
import com.zhangyuan.user.dto.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.dev33.satoken.stp.StpUtil;

import java.util.UUID;

@Service
public class SaasUserApplicationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SaasUserApplicationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname() != null ? request.nickname() : request.email().split("@")[0]
        );
        user.setApiKey("sk-" + UUID.randomUUID().toString().replace("-", ""));
        user = userRepository.save(user);
        StpUtil.login("saas_" + user.getId());
        return new LoginResponse(StpUtil.getTokenValue(), toResponse(user));
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        StpUtil.login("saas_" + user.getId());
        return new LoginResponse(StpUtil.getTokenValue(), toResponse(user));
    }

    public UserResponse getProfile(Long userId) {
        return toResponse(userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    public ApiResponse<Long> verifyApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .map(user -> {
                    if (!user.isActive()) {
                        return ApiResponse.<Long>error(403, "User is not active");
                    }
                    return ApiResponse.<Long>ok(user.getId());
                })
                .orElse(ApiResponse.<Long>error(404, "API Key not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getNickname(),
                user.getApiKey(), user.getQuotaUsed(), user.getQuotaLimit(), user.getStatus()
        );
    }
}
