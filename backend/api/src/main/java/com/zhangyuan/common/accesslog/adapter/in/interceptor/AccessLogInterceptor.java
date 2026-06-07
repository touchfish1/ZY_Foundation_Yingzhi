package com.zhangyuan.common.accesslog.adapter.in.interceptor;

import com.zhangyuan.common.accesslog.application.AccessLogService;
import com.zhangyuan.common.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AccessLogInterceptor.class);
    private static final String START_TIME_ATTR = "accessLog.startTime";

    private final AccessLogService accessLogService;

    public AccessLogInterceptor(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) {
            return;
        }
        long durationMs = System.currentTimeMillis() - startTime;

        String requestMethod = request.getMethod();
        String requestPath = request.getRequestURI();
        int responseStatus = response.getStatus();

        // Skip health checks and static resources to reduce noise
        if (shouldSkip(requestPath)) {
            return;
        }

        // Get current user from SecurityContext
        Long userId = null;
        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser user) {
            userId = user.getId();
            username = user.getNickname();
        }

        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        try {
            accessLogService.record(requestMethod, requestPath, responseStatus,
                    userId, username, ipAddress, userAgent, durationMs);
        } catch (Exception e) {
            log.warn("Failed to record access log: {}", e.getMessage());
        }
    }

    private boolean shouldSkip(String path) {
        return path.startsWith("/actuator/health") ||
               path.startsWith("/static/") ||
               path.startsWith("/favicon.ico") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
