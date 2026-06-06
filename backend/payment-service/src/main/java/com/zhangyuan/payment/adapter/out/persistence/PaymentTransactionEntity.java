package com.zhangyuan.payment.adapter.out.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransactionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payment_no", unique = true, nullable = false, length = 64)
    private String paymentNo;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;
    @Column(nullable = false, length = 32)
    private String channel;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(length = 8)
    private String currency = "CNY";
    @Column(length = 32)
    private String status = "PENDING";
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_json", columnDefinition = "jsonb")
    private String requestJson;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "callback_json", columnDefinition = "jsonb")
    private String callbackJson;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "paid_at")
    private Instant paidAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRequestJson() { return requestJson; }
    public void setRequestJson(String requestJson) { this.requestJson = requestJson; }
    public String getCallbackJson() { return callbackJson; }
    public void setCallbackJson(String callbackJson) { this.callbackJson = callbackJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}
