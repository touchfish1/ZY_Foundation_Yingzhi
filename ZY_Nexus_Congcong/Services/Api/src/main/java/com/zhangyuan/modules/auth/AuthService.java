package com.zhangyuan.modules.auth;

import com.zhangyuan.common.security.AuthUser;
import com.zhangyuan.common.security.AuthUserService;
import com.zhangyuan.common.security.JwtService;
import com.zhangyuan.common.security.SecurityProperties;
import com.zhangyuan.modules.auth.dto.AdminUserResponse;
import com.zhangyuan.modules.auth.dto.LoginResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

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

    public LoginResponse login(String username, String password) {
        AuthUser user = (AuthUser) authUserService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Admin user is disabled");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, securityProperties.getJwtExpiresSeconds(), toResponse(user), user.getPermissions());
    }

    public LoginResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser user = (AuthUser) authentication.getPrincipal();
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
