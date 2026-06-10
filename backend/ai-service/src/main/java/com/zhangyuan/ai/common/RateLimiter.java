package com.zhangyuan.ai.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based rate limiter using concurrency counter and RPM sliding window.
 * <p>
 * Concurrency: uses INCR/DECR on key "ratelimit:concurrency:{userId}" with a TTL safety net.
 * RPM: uses a Redis sorted set "ratelimit:rpm:{userId}" with millisecond-precision scores for
 * a true sliding window (any 60-second window, not fixed clock minutes).
 */
@Service
public class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    private static final String CONCURRENCY_PREFIX = "ratelimit:concurrency:";
    private static final String RPM_PREFIX = "ratelimit:rpm:";
    private static final int RPM_WINDOW_SECONDS = 60;
    private static final int TTL_SECONDS = 120;

    private final StringRedisTemplate redisTemplate;

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Attempt to acquire a rate limit slot.
     * <p>
     * If the request has already been rate-limited upstream (e.g. by RateLimitFilter),
     * this is a no-op and returns true immediately.
     *
     * @param userId           the user identifier
     * @param cost             cost for RPM (typically 1 per request)
     * @param rpmLimit         max requests per 60-second sliding window
     * @param concurrencyLimit max concurrent requests
     * @return true if acquired, false if rate limited
     */
    public boolean tryAcquire(Long userId, int cost, int rpmLimit, int concurrencyLimit) {
        // Guard: if already acquired upstream (e.g. by RateLimitFilter), skip Redis ops
        ApiKeyContext ctx = ApiKeyContext.get();
        if (ctx != null && ctx.isAcquired()) {
            return true;
        }

        if (userId == null) {
            return true;
        }

        // Check concurrency first
        String concurrencyKey = CONCURRENCY_PREFIX + userId;
        Long currentConcurrency = redisTemplate.opsForValue().increment(concurrencyKey);
        if (currentConcurrency == null) {
            log.warn("Redis INCR returned null for key: {}", concurrencyKey);
            return true;
        }

        redisTemplate.expire(concurrencyKey, TTL_SECONDS, TimeUnit.SECONDS);

        if (currentConcurrency > concurrencyLimit) {
            redisTemplate.opsForValue().decrement(concurrencyKey);
            log.warn("Concurrency limit exceeded for userId={}: {} > {}", userId, currentConcurrency, concurrencyLimit);
            return false;
        }

        String rpmKey = RPM_PREFIX + userId;
        long now = System.currentTimeMillis();
        long windowStart = now - (RPM_WINDOW_SECONDS * 1000L);

        redisTemplate.opsForZSet().removeRangeByScore(rpmKey, 0, windowStart);

        String member = now + ":" + UUID.randomUUID().toString().substring(0, 8);
        redisTemplate.opsForZSet().add(rpmKey, member, now);

        Long count = redisTemplate.opsForZSet().count(rpmKey, windowStart, now + 1);
        if (count == null) {
            log.warn("Redis ZCOUNT returned null for key: {}", rpmKey);
            return true;
        }

        redisTemplate.expire(rpmKey, (long) RPM_WINDOW_SECONDS + TTL_SECONDS, TimeUnit.SECONDS);

        if (count > rpmLimit) {
            redisTemplate.opsForZSet().remove(rpmKey, member);
            redisTemplate.opsForValue().decrement(concurrencyKey);
            log.warn("RPM limit exceeded for userId={}: {} > {}", userId, count, rpmLimit);
            return false;
        }

        if (ctx != null) {
            ctx.markAcquired();
        }
        return true;
    }

    /**
     * Release a concurrency slot after request completion (success or error).
     * <p>
     * If the release was already performed upstream (e.g. by RateLimitFilter),
     * this is a no-op based on the ApiKeyContext guard.
     *
     * @param userId the user identifier
     */
    public void release(Long userId) {
        ApiKeyContext ctx = ApiKeyContext.get();
        // Only the first caller (filter or service) performs the actual DECR
        if (ctx != null && !ctx.isAcquired()) {
            return;
        }
        if (userId == null) {
            return;
        }
        String concurrencyKey = CONCURRENCY_PREFIX + userId;
        redisTemplate.opsForValue().decrement(concurrencyKey);
        if (ctx != null) {
            ctx.markReleased();
        }
    }

    /**
     * Get current rate limit status headers for a user.
     *
     * @param userId  the user identifier
     * @param rpmLimit the RPM limit for this user
     * @return map of header name to header value
     */
    public Map<String, String> getHeaders(Long userId, int rpmLimit) {
        Map<String, String> headers = new HashMap<>();
        if (userId == null) {
            headers.put("X-RateLimit-Limit", String.valueOf(rpmLimit));
            headers.put("X-RateLimit-Remaining", String.valueOf(rpmLimit));
            headers.put("X-RateLimit-Reset", "0");
            return headers;
        }

        String rpmKey = RPM_PREFIX + userId;
        long now = System.currentTimeMillis();
        long windowStart = now - (RPM_WINDOW_SECONDS * 1000L);

        // Trim old entries and count
        redisTemplate.opsForZSet().removeRangeByScore(rpmKey, 0, windowStart);
        Long count = redisTemplate.opsForZSet().count(rpmKey, windowStart, now + 1);

        int remaining = Math.max(0, rpmLimit - (count != null ? count.intValue() : 0));
        long resetTime = now / 1000 + RPM_WINDOW_SECONDS;

        // Find the timestamp of the oldest entry to compute precise reset time
        var oldest = redisTemplate.opsForZSet().rangeByScore(rpmKey, windowStart, now, 0, 1);
        if (oldest != null && !oldest.isEmpty()) {
            Double oldestScore = redisTemplate.opsForZSet().score(rpmKey, oldest.iterator().next());
            if (oldestScore != null) {
                resetTime = (long) (oldestScore / 1000) + RPM_WINDOW_SECONDS;
            }
        }

        headers.put("X-RateLimit-Limit", String.valueOf(rpmLimit));
        headers.put("X-RateLimit-Remaining", String.valueOf(remaining));
        headers.put("X-RateLimit-Reset", String.valueOf(resetTime));
        return headers;
    }
}
