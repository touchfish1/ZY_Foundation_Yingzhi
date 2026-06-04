package com.zhangyuan.modules.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_price")
public class ProductPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(nullable = false, length = 16)
    private String currency = "CNY";

    @Column(name = "billing_cycle", nullable = false, length = 32)
    private String billingCycle;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "original_amount")
    private BigDecimal originalAmount;

    @Column(nullable = false, length = 32)
    private String status = "enabled";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected ProductPrice() {
    }

    public ProductPrice(Long planId, String currency, String billingCycle, BigDecimal amount, BigDecimal originalAmount) {
        this.planId = planId;
        this.currency = currency;
        this.billingCycle = billingCycle;
        this.amount = amount;
        this.originalAmount = originalAmount;
    }

    public Long getId() {
        return id;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public String getStatus() {
        return status;
    }
}
