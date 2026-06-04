package com.zhangyuan.user.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private BigDecimal balance = BigDecimal.ZERO;
    private int concurrency = 5;
    private int rpmLimit;
    private String role = "user";
    private String status = "active";
    private String apiKey;
    private BigDecimal totalRecharged = BigDecimal.ZERO;
    private Long quotaUsed = 0L;
    private Long quotaLimit = 0L;
    private String notes;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {}

    public User(String email, String passwordHash, String nickname) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
    }

    public void recharge(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.totalRecharged = this.totalRecharged.add(amount);
    }

    public void deduct(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("余额不足");
        }
        this.balance = this.balance.subtract(amount);
    }

    public boolean isActive() { return "active".equals(this.status); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public int getConcurrency() { return concurrency; }
    public void setConcurrency(int concurrency) { this.concurrency = concurrency; }
    public int getRpmLimit() { return rpmLimit; }
    public void setRpmLimit(int rpmLimit) { this.rpmLimit = rpmLimit; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public BigDecimal getTotalRecharged() { return totalRecharged; }
    public void setTotalRecharged(BigDecimal totalRecharged) { this.totalRecharged = totalRecharged; }
    public Long getQuotaUsed() { return quotaUsed; }
    public void setQuotaUsed(Long quotaUsed) { this.quotaUsed = quotaUsed; }
    public Long getQuotaLimit() { return quotaLimit; }
    public void setQuotaLimit(Long quotaLimit) { this.quotaLimit = quotaLimit; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
