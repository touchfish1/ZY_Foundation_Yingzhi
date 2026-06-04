package com.zhangyuan.common.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthUserService {

    private static final Logger log = LoggerFactory.getLogger(AuthUserService.class);

    public AuthUser loadUserByUsername(String username) {
        throw new UnsupportedOperationException(
                "loadUserByUsername is not available in this service. Use auth-service for login.");
    }

    @SuppressWarnings("unchecked")
    public AuthUser loadUserById(Long id) {
        if (!StpUtil.isLogin()) {
            throw new IllegalStateException("No authenticated user found");
        }
        long loginId = StpUtil.getLoginIdAsLong();
        if (loginId != id) {
            throw new SecurityException("Authenticated user ID mismatch");
        }
        SaSession session = StpUtil.getSession();

        List<String> permissions = (List<String>) session.get("permissions", new ArrayList<String>());
        String username = session.getString("username");
        String nickname = session.getString("nickname");

        return new AuthUser(id, username, null, nickname, permissions, true);
    }

    @SuppressWarnings("unchecked")
    public List<String> findRoleCodes(String username) {
        if (!StpUtil.isLogin()) return List.of();
        SaSession session = StpUtil.getSession();
        return (List<String>) session.get("roleCodes", new ArrayList<String>());
    }
}
