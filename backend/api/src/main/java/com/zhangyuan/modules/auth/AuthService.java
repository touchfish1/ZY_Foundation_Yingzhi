package com.zhangyuan.modules.auth;

import com.zhangyuan.common.security.AuthUser;
import com.zhangyuan.common.security.AuthUserService;
import com.zhangyuan.common.security.JwtService;
import com.zhangyuan.common.security.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zhangyuan.modules.auth.dto.AdminUserResponse;
import com.zhangyuan.modules.auth.dto.LoginResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务，提供用户登录和当前用户信息查询功能。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthUserService authUserService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    public AuthService(AuthUserService authUserService, JwtService jwtService, PasswordEncoder passwordEncoder, SecurityProperties securityProperties) {
        this.authUserService = authUserService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
    }

    /**
     * 用户登录认证，验证密码和账号状态后生成 JWT 令牌。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 登录响应（含令牌和用户信息）
     * @throws BadCredentialsException 用户名密码错误或账号禁用时抛出
     */
    public LoginResponse login(String username, String password) {
        log.info("User login: username={}", username);
        AuthUser user = (AuthUser) authUserService.loadUserByUsername(username);
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for user: {}", username);
            throw new BadCredentialsException("Invalid username or password");
        }
        // 检查账号是否启用
        if (!user.isEnabled()) {
            log.warn("Disabled user attempted login: {}", username);
            throw new BadCredentialsException("Admin user is disabled");
        }

        String token = jwtService.generateToken(user);
        log.info("User logged in: username={}", username);
        return new LoginResponse(token, securityProperties.getJwtExpiresSeconds(), toResponse(user), user.getPermissions());
    }

    /**
     * 获取当前已登录用户的信息（不含令牌）。
     *
     * @return 当前用户信息
     */
    public LoginResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser user = (AuthUser) authentication.getPrincipal();
        log.info("Getting current user: username={}", user.getUsername());
        return new LoginResponse(null, 0, toResponse(user), user.getPermissions());
    }

    /**
     * 将 AuthUser 转换为 AdminUserResponse 响应对象。
     */
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
