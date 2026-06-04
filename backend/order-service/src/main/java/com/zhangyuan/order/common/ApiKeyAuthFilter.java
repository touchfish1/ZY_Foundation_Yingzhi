package com.zhangyuan.order.common;

import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.order.client.UserServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private final UserServiceClient userServiceClient;

    public ApiKeyAuthFilter(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        String path = request.getRequestURI();

        // Only protect /api/orders paths
        if (!path.startsWith("/api/orders") || path.contains("fulfill")) {
            try { filterChain.doFilter(request, response); } catch (Exception e) { log.error("Filter error", e); }
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":401,\"message\":\"Missing token\",\"data\":null}");
            return;
        }

        String token = authHeader.substring(7);

        // Check if it's a valid Sa-Token session first (web users)
        StpUtil.setTokenValue(token);
        if (StpUtil.isLogin()) {
            request.setAttribute("userId", StpUtil.getLoginIdAsLong());
            try { filterChain.doFilter(request, response); } catch (Exception e) { log.error("Filter error", e); }
            return;
        }

        // Fallback: verify as API key via system-service
        try {
            var resp = userServiceClient.verifyApiKey(token);
            if (resp == null || resp.code() != 0) {
                response.setStatus(403);
                response.getWriter().write("{\"code\":403,\"message\":\"Invalid token\",\"data\":null}");
                return;
            }
            request.setAttribute("userId", resp.data());
        } catch (Exception e) {
            log.error("API Key verification failed", e);
            response.setStatus(500);
            response.getWriter().write("{\"code\":500,\"message\":\"Auth service unavailable\",\"data\":null}");
            return;
        }

        try { filterChain.doFilter(request, response); } catch (Exception e) { log.error("Filter error", e); }
    }
}
