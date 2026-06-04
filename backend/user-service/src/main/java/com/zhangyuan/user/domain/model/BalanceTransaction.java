package com.zhangyuan.user.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class BalanceTransaction {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String transactionType;
    private String description;
    private Instant createdAt;

    public BalanceTransaction() {}

    public BalanceTransaction(Long userId, BigDecimal amount, BigDecimal balanceAfter, String transactionType, String description) {
        this.userId = userId;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.transactionType = transactionType;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
