package com.zhangyuan.ai.common;

import com.zhangyuan.ai.client.UserServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet filter that enforces Redis-based rate limits on /v1/** endpoints.
 * <p>
 * 1. Extracts userId from the API key via UserServiceClient
 * 2. Stores context in {@link ApiKeyContext} for downstream use
 * 3. Calls {@link RateLimiter#tryAcquire} to enforce concurrency + RPM limits
 * 4. Returns 429 with X-RateLimit-* headers when limits are exceeded
 * 5. Guarantees {@link RateLimiter#release} is called on every response path
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int DEFAULT_CONCURRENCY = 5;
    private static final int DEFAULT_RPM = 60;

    private final RateLimiter rateLimiter;
    private final UserServiceClient userServiceClient;

    public RateLimitFilter(RateLimiter rateLimiter, UserServiceClient userServiceClient) {
        this.rateLimiter = rateLimiter;
        this.userServiceClient = userServiceClient;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Long userId = null;
        try {
            userId = resolveUserId(request);
        } catch (Exception e) {
            log.debug("Could not resolve userId for rate limiting: {}", e.getMessage());
        }

        if (userId == null) {
            chain.doFilter(request, response);
            return;
        }

        ApiKeyContext.set(userId, DEFAULT_CONCURRENCY, DEFAULT_RPM);

        boolean acquired = rateLimiter.tryAcquire(userId, 1, DEFAULT_RPM, DEFAULT_CONCURRENCY);

        if (acquired) {
            try {
                chain.doFilter(request, response);
            } finally {
                rateLimiter.release(userId);
                ApiKeyContext.clear();
            }
        } else {
            sendRateLimitResponse(response, userId, DEFAULT_RPM);
            ApiKeyContext.clear();
        }
    }

    /**
     * Extract userId from the Authorization Bearer token by verifying it
     * against the user service. Returns null if the key cannot be resolved
     * (auth errors are handled downstream by the controller).
     */
    private Long resolveUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String apiKey = authHeader.substring(7);
        var resp = userServiceClient.verifyApiKey(apiKey);
        if (resp == null || resp.code() != 0 || resp.data() == null) {
            return null;
        }
        Object rawUserId = resp.data().get("userId");
        if (rawUserId == null) {
            return null;
        }
        return ((Number) rawUserId).longValue();
    }

    private void sendRateLimitResponse(HttpServletResponse response,
                                       Long userId, int rpmLimit) throws IOException {
        Map<String, String> headers = rateLimiter.getHeaders(userId, rpmLimit);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.setHeader(entry.getKey(), entry.getValue());
        }
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        String body = "{\"error\":{\"message\":\"Rate limit exceeded. Please wait and retry.\",\"type\":\"rate_limit_exceeded\"}}";
        response.getWriter().write(body);
    }
}
