package com.zhangyuan.auth.common.security;

import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.auth.common.security.AuthUser;
import com.zhangyuan.auth.common.security.AuthUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SaTokenSecurityContextBridge implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SaTokenSecurityContextBridge.class);

    private final AuthUserService authUserService;

    public SaTokenSecurityContextBridge(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (StpUtil.isLogin()) {
            try {
                long loginId = StpUtil.getLoginIdAsLong();
                AuthUser user = authUserService.loadUserById(loginId);
                var authorities = user.getPermissions().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.warn("Failed to bridge Sa-Token to SecurityContext: {}", e.getMessage());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityContextHolder.clearContext();
    }
}
