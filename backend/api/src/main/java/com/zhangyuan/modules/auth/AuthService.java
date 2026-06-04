package com.zhangyuan.modules.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.common.security.AuthUser;
import com.zhangyuan.common.security.AuthUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zhangyuan.modules.auth.dto.AdminUserResponse;
import com.zhangyuan.modules.auth.dto.LoginResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthUserService authUserService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthUserService authUserService, PasswordEncoder passwordEncoder) {
        this.authUserService = authUserService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(String username, String password) {
        log.info("User login: username={}", username);
        AuthUser user = authUserService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for user: {}", username);
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!user.isEnabled()) {
            log.warn("Disabled user attempted login: {}", username);
            throw new BadCredentialsException("Admin user is disabled");
        }

        StpUtil.login(user.getId());
        String tokenValue = StpUtil.getTokenValue();

        log.info("User logged in: username={}", username);
        return new LoginResponse(tokenValue, (int) StpUtil.getTokenTimeout(), toResponse(user), user.getPermissions());
    }

    public LoginResponse currentUser() {
        long loginId = StpUtil.getLoginIdAsLong();
        AuthUser user = authUserService.loadUserById(loginId);
        log.info("Getting current user: username={}", user.getUsername());
        return new LoginResponse(null, 0, toResponse(user), user.getPermissions());
    }

    private AdminUserResponse toResponse(AuthUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                null,
                authUserService.findRoleCodes(user.getUsername())
        );
    }
}
