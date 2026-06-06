package com.zhangyuan.payment.domain.model;

import java.time.Instant;

public class CompensationEvent {

    private Long id;
    private String paymentNo;
    private String eventType;
    private String eventStatus = "PENDING";
    private int retryCount;
    private int maxRetries = 5;
    private String payloadJson;
    private String lastError;
    private Instant createdAt;
    private Instant lastRetryAt;

    public CompensationEvent(String paymentNo, String eventType, String payloadJson) {
        this.paymentNo = paymentNo;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.createdAt = Instant.now();
    }

    public void markProcessed() {
        this.eventStatus = "PROCESSED";
    }

    public void markFailed(String error) {
        this.eventStatus = "FAILED";
        this.lastError = error;
        this.lastRetryAt = Instant.now();
    }

    public void incrementRetry(String error) {
        this.retryCount++;
        this.lastError = error;
        this.lastRetryAt = Instant.now();
        if (this.retryCount >= this.maxRetries) {
            this.eventStatus = "FAILED";
        }
    }

    public boolean isExpired() {
        return retryCount >= maxRetries;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentNo() { return paymentNo; }
    public String getEventType() { return eventType; }
    public String getEventStatus() { return eventStatus; }
    public void setEventStatus(String eventStatus) { this.eventStatus = eventStatus; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastRetryAt() { return lastRetryAt; }
    public void setLastRetryAt(Instant lastRetryAt) { this.lastRetryAt = lastRetryAt; }
}
