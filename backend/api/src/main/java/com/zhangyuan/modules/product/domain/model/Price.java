package com.zhangyuan.modules.product.domain.model;

import com.zhangyuan.common.dddframework.Money;
import java.math.BigDecimal;

public class Price {

    private Long id;
    private Plan plan;
    private String currency;
    private String billingCycle;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private boolean enabled;

    public Price(Plan plan, String currency, String billingCycle, BigDecimal amount, BigDecimal originalAmount) {
        this.plan = plan;
        this.currency = currency;
        this.billingCycle = billingCycle;
        this.amount = amount;
        this.originalAmount = originalAmount;
        this.enabled = true;
    }

    public Money asMoney() {
        return Money.of(amount.toString(), currency);
    }

    public void updateAmount(BigDecimal amount, BigDecimal originalAmount) {
        this.amount = amount;
        this.originalAmount = originalAmount;
    }

    public void disable() { this.enabled = false; }

    public Long getId() { return id; }
    public Plan getPlan() { return plan; }
    public String getCurrency() { return currency; }
    public String getBillingCycle() { return billingCycle; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public boolean isEnabled() { return enabled; }
}
