package com.zhangyuan.system.application.service;

import com.zhangyuan.system.adapter.out.persistence.SaasUserEntity;
import com.zhangyuan.system.dto.*;
import com.zhangyuan.system.repository.SaasUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.dev33.satoken.stp.StpUtil;

import java.util.UUID;

@Service
public class SaasUserApplicationService {
    private final SaasUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SaasUserApplicationService(SaasUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        SaasUserEntity user = new SaasUserEntity(
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
        SaasUserEntity user = userRepository.findByEmail(request.email())
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

    private UserResponse toResponse(SaasUserEntity user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getNickname(),
                user.getApiKey(), user.getQuotaUsed(), user.getQuotaLimit(), user.getStatus()
        );
    }
}
