package com.zhangyuan.user.adapter.out.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "balance_transaction")
public class BalanceTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 20, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "transaction_type", nullable = false, length = 32)
    private String transactionType;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public BalanceTransactionEntity() {}

    public BalanceTransactionEntity(Long userId, BigDecimal amount, BigDecimal balanceAfter, String transactionType, String description) {
        this.userId = userId;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.transactionType = transactionType;
        this.description = description;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getTransactionType() { return transactionType; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
