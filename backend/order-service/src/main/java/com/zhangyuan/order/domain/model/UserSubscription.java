package com.zhangyuan.order.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class UserSubscription {
    private Long id;
    private Long userId;
    private String planCode;
    private String planName;
    private String status;  // active, expired, suspended
    private Instant startsAt;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    public UserSubscription() {}

    public UserSubscription(Long userId, String planCode, String planName, int validityDays) {
        this.userId = userId;
        this.planCode = planCode;
        this.planName = planName;
        this.status = "active";
        this.startsAt = Instant.now();
        this.expiresAt = this.startsAt.plus(validityDays, ChronoUnit.DAYS);
    }

    /** Extend subscription by adding days to current expiry */
    public void extend(int days) {
        if ("expired".equals(this.status) || Instant.now().isAfter(this.expiresAt)) {
            this.expiresAt = Instant.now().plus(days, ChronoUnit.DAYS);
            this.status = "active";
        } else {
            this.expiresAt = this.expiresAt.plus(days, ChronoUnit.DAYS);
        }
    }

    public boolean isActive() {
        return "active".equals(this.status) && Instant.now().isBefore(this.expiresAt);
    }

    public void suspend() { this.status = "suspended"; }
    public void activate() { this.status = "active"; }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getStartsAt() { return startsAt; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
