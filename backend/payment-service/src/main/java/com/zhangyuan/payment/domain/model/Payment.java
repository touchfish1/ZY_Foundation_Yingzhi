package com.zhangyuan.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment {

    private Long id;
    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private String channel;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String requestJson;
    private String callbackJson;
    private Instant createdAt;
    private Instant paidAt;

    public Payment() {}

    public Payment(String paymentNo, Long orderId, String orderNo, String channel, BigDecimal amount, String currency) {
        this.paymentNo = paymentNo;
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.channel = channel;
        this.amount = amount;
        this.currency = currency;
        this.status = "PENDING";
        this.createdAt = Instant.now();
    }

    public void markSuccess() {
        this.status = "SUCCESS";
        this.paidAt = Instant.now();
    }

    public void markFailed() {
        this.status = "FAILED";
    }

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
