package com.zhangyuan.modules.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment {

    private Long id;
    private String paymentNo;
    private Long orderId;
    private String channel;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant paidAt;

    public Payment(String paymentNo, Long orderId, String channel, BigDecimal amount, String currency) {
        this.paymentNo = paymentNo;
        this.orderId = orderId;
        this.channel = channel;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markSuccess() {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Cannot mark payment as success. Current status: " + status);
        }
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = Instant.now();
    }

    public void markFailed() {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Cannot mark payment as failed. Current status: " + status);
        }
        this.status = PaymentStatus.FAILED;
    }

    public boolean isSuccess() { return status == PaymentStatus.SUCCESS; }
    public boolean isPending() { return status == PaymentStatus.PENDING; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentNo() { return paymentNo; }
    public Long getOrderId() { return orderId; }
    public String getChannel() { return channel; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }
}
