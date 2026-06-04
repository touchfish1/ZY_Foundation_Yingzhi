package com.zhangyuan.modules.order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {

    private Long id;
    private OrderNumber orderNo;
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
    public OrderNumber getOrderNo() { return orderNo; }
    public Long getPlanId() { return planId; }
    public Long getPriceId() { return priceId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public OrderStatus getStatus() { return status; }
    public String getSnapshotJson() { return snapshotJson; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCancelledAt() { return cancelledAt; }
}
