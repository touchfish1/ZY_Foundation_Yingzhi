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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private final UserServiceClient userServiceClient;

    // Per-user RPM counters (sliding window)
    private final ConcurrentHashMap<Long, SlidingWindowCounter> rpmCounters = new ConcurrentHashMap<>();

    public ApiKeyAuthFilter(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        String path = request.getRequestURI();

        if (!path.startsWith("/api/orders") || path.contains("fulfill")) {
            try { filterChain.doFilter(request, response); } catch (Exception e) { log.error("Filter error", e); }
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeJson(response, 401, "Missing token");
            return;
        }

        String token = authHeader.substring(7);

        StpUtil.setTokenValue(token);
        if (StpUtil.isLogin()) {
            request.setAttribute("userId", StpUtil.getLoginIdAsLong());
            try { filterChain.doFilter(request, response); } catch (Exception e) { log.error("Filter error", e); }
            return;
        }

        try {
            var resp = userServiceClient.verifyApiKeyWithQuota(token);
            if (resp == null || resp.code() != 0 || resp.data() == null) {
                writeJson(response, 403, "Invalid API key");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> quota = (Map<String, Object>) resp.data();
            Long userId = ((Number) quota.get("userId")).longValue();
            request.setAttribute("userId", userId);

            if (!checkQuota(response, quota)) {
                return;
            }

            if (!checkRpmLimit(response, userId, quota)) {
                return;
            }

        } catch (Exception e) {
            log.error("API Key verification failed", e);
            writeJson(response, 500, "Auth service unavailable");
            return;
        }

        try { filterChain.doFilter(request, response); } catch (Exception e) { log.error("Filter error", e); }
    }

    private boolean checkQuota(HttpServletResponse response, Map<String, Object> quota) throws IOException {
        Long userId = ((Number) quota.get("userId")).longValue();
        long quotaUsed = quota.get("quotaUsed") != null ? ((Number) quota.get("quotaUsed")).longValue() : 0;
        long quotaLimit = quota.get("quotaLimit") != null ? ((Number) quota.get("quotaLimit")).longValue() : 0;
        if (quotaLimit > 0 && quotaUsed >= quotaLimit) {
            log.warn("Quota exhausted for userId={}: used={}, limit={}", userId, quotaUsed, quotaLimit);
            writeJson(response, 429, "Quota exhausted");
            return false;
        }
        return true;
    }

    private boolean checkRpmLimit(HttpServletResponse response, Long userId, Map<String, Object> quota) throws IOException {
        int rpmLimit = quota.get("rpmLimit") != null ? ((Number) quota.get("rpmLimit")).intValue() : 0;
        if (rpmLimit <= 0) {
            return true;
        }
        SlidingWindowCounter counter = rpmCounters.computeIfAbsent(userId, k -> new SlidingWindowCounter());
        long currentCount = counter.incrementAndGet();
        if (currentCount > rpmLimit) {
            log.warn("RPM limit exceeded for userId={}: {} > {}", userId, currentCount, rpmLimit);
            writeJson(response, 429, "Rate limit exceeded");
            return false;
        }
        return true;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}");
    }

    private static class SlidingWindowCounter {
        private final AtomicLong count = new AtomicLong(0);
        private final long windowStart = System.currentTimeMillis();

        long incrementAndGet() {
            long elapsed = System.currentTimeMillis() - windowStart;
            if (elapsed > 60_000) {
                count.set(1);
                return 1;
            }
            return count.incrementAndGet();
        }
    }
}
