package com.zhangyuan.modules.payment.adapter.out.persistence;

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
@Table(name = "payment_transaction")
public class PaymentTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_no", nullable = false, unique = true, length = 64)
    private String paymentNo;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 64)
    private String channel;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 16)
    private String currency;

    @Column(nullable = false, length = 32)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_json", columnDefinition = "jsonb")
    private String requestJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "callback_json", columnDefinition = "jsonb")
    private String callbackJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "paid_at")
    private Instant paidAt;

    protected PaymentTransactionEntity() {
    }

    public PaymentTransactionEntity(String paymentNo, Long orderId, String channel, BigDecimal amount, String currency, String requestJson) {
        this.paymentNo = paymentNo;
        this.orderId = orderId;
        this.channel = channel;
        this.amount = amount;
        this.currency = currency;
        this.status = "pending";
        this.requestJson = requestJson;
    }

    public void markPaid(String callbackJson, Instant paidAt) {
        this.status = "paid";
        this.callbackJson = callbackJson;
        this.paidAt = paidAt;
    }

    public Long getId() {
        return id;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getChannel() {
        return channel;
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

    public String getRequestJson() {
        return requestJson;
    }

    public String getCallbackJson() {
        return callbackJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }
}
