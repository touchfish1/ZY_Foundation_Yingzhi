package com.zhangyuan.modules.order.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_main")
public class OrderMainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "price_id", nullable = false)
    private Long priceId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 16)
    private String currency;

    @Column(nullable = false, length = 32)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "jsonb")
    private String snapshotJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    protected OrderMainEntity() {
    }

    public OrderMainEntity(String orderNo, Long planId, Long priceId, BigDecimal amount, String currency, String snapshotJson) {
        this.orderNo = orderNo;
        this.planId = planId;
        this.priceId = priceId;
        this.amount = amount;
        this.currency = currency;
        this.status = "pending";
        this.snapshotJson = snapshotJson;
    }

    public void markPaid(Instant paidAt) {
        this.status = "paid";
        this.paidAt = paidAt;
    }

    public void markCancelled(Instant cancelledAt) {
        this.status = "cancelled";
        this.cancelledAt = cancelledAt;
    }

    public void markExpired() {
        this.status = "expired";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPlanId() {
        return planId;
    }

    public Long getPriceId() {
        return priceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }
}
