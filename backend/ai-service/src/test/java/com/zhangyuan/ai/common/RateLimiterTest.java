package com.zhangyuan.ai.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RateLimiterTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    private final ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);
        rateLimiter = new RateLimiter(redisTemplate);
        // Clear ThreadLocal context to prevent cross-test contamination
        ApiKeyContext.clear();
    }

    @Test
    void tryAcquire_withinLimit_returnsTrue() {
        // Concurrency: 1 concurrent (= within 5 limit)
        when(valueOps.increment(anyString())).thenReturn(1L);
        // RPM: first request in window
        when(zSetOps.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOps.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOps.count(anyString(), anyDouble(), anyDouble())).thenReturn(1L);

        assertThat(rateLimiter.tryAcquire(1L, 1, 60, 5)).isTrue();
    }

    @Test
    void tryAcquire_concurrencyExceeded_returnsFalse() {
        // Concurrency: 6 concurrent (= exceeds 5 limit)
        when(valueOps.increment(anyString())).thenReturn(6L);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        boolean result = rateLimiter.tryAcquire(1L, 1, 60, 5);

        assertThat(result).isFalse();
        // Verify cleanup: decrement concurrency counter
        verify(valueOps).decrement(anyString());
    }

    @Test
    void tryAcquire_rpmExceeded_returnsFalse() {
        // Concurrency within limit
        when(valueOps.increment(anyString())).thenReturn(1L);
        // RPM: count = 61 (> 60 limit)
        when(zSetOps.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOps.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOps.count(anyString(), anyDouble(), anyDouble())).thenReturn(61L);

        boolean result = rateLimiter.tryAcquire(1L, 1, 60, 5);

        assertThat(result).isFalse();
        // Verify cleanup: remove RPM entry AND decrement concurrency
        verify(zSetOps).remove(anyString(), anyString());
        verify(valueOps).decrement(anyString());
    }

    @Test
    void tryAcquire_nullUserId_returnsTrue() {
        // With null userId, the method should return true immediately (skip Redis ops)
        boolean result = rateLimiter.tryAcquire(null, 1, 60, 5);

        assertThat(result).isTrue();
        // Verify no Redis operations were called
        verifyNoInteractions(valueOps, zSetOps);
    }
}
