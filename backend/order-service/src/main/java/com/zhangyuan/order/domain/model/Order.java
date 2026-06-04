package com.zhangyuan.order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {

    private Long id;
    private OrderNumber orderNo;
    private Long userId;
    private Long planId;
    private Long priceId;
    private BigDecimal amount;
    private String currency;
    private OrderStatus status;
    private String snapshotJson;
    private Instant createdAt;
    private Instant paidAt;
    private Instant cancelledAt;

    public Order(OrderNumber orderNo, Long planId, Long priceId, BigDecimal amount, String currency, String snapshotJson) {
        this.orderNo = orderNo;
        this.planId = planId;
        this.priceId = priceId;
        this.amount = amount;
        this.currency = currency;
        this.snapshotJson = snapshotJson;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markPaid() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be paid. Current status: " + status);
        }
        this.status = OrderStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void cancel() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be cancelled. Current status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public boolean isPaid() { return status == OrderStatus.PAID; }
    public boolean isPending() { return status == OrderStatus.PENDING; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OrderNumber getOrderNo() { return orderNo; }
    public void setOrderNo(OrderNumber orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public Long getPriceId() { return priceId; }
    public void setPriceId(Long priceId) { this.priceId = priceId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getSnapshotJson() { return snapshotJson; }
    public void setSnapshotJson(String snapshotJson) { this.snapshotJson = snapshotJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
}
