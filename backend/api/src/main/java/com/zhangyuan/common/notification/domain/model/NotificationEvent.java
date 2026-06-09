package com.zhangyuan.common.notification.domain.model;

import java.time.Instant;
import java.util.Map;

public class NotificationEvent {
    private final String type;
    private final Long userId;
    private final String email;
    private final Map<String, Object> payload;
    private final Instant createdAt;

    public NotificationEvent(String type, Long userId, String email, Map<String, Object> payload) {
        this.type = type;
        this.userId = userId;
        this.email = email;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public String getType() { return type; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public Map<String, Object> getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
}
