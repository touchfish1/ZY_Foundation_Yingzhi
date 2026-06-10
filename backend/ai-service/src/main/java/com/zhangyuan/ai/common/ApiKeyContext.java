package com.zhangyuan.ai.common;

/**
 * ThreadLocal holder for the current request's API key context.
 * Set by RateLimitFilter before processing, cleared after request completes.
 * <p>
 * The {@code acquireCount} field tracks whether {@link RateLimiter#tryAcquire} has
 * already been called for this request (by the filter), so that a second call from
 * {@code ChatProxyService} does not double-count Redis operations.
 */
public class ApiKeyContext {

    private static final ThreadLocal<ApiKeyContext> CONTEXT = ThreadLocal.withInitial(ApiKeyContext::new);

    private Long userId;
    private int concurrencyLimit = 5;
    private int rpmLimit = 60;
    /** 0 = not acquired, >0 = already acquired (prevents double Redis ops) */
    private int acquireCount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getConcurrencyLimit() {
        return concurrencyLimit;
    }

    public void setConcurrencyLimit(int concurrencyLimit) {
        this.concurrencyLimit = concurrencyLimit;
    }

    public int getRpmLimit() {
        return rpmLimit;
    }

    public void setRpmLimit(int rpmLimit) {
        this.rpmLimit = rpmLimit;
    }

    public boolean isAcquired() {
        return acquireCount > 0;
    }

    public void markAcquired() {
        this.acquireCount++;
    }

    public void markReleased() {
        this.acquireCount = 0;
    }

    public static ApiKeyContext get() {
        return CONTEXT.get();
    }

    public static void set(Long userId, int concurrencyLimit, int rpmLimit) {
        ApiKeyContext ctx = CONTEXT.get();
        ctx.setUserId(userId);
        ctx.setConcurrencyLimit(concurrencyLimit);
        ctx.setRpmLimit(rpmLimit);
        ctx.acquireCount = 0;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
