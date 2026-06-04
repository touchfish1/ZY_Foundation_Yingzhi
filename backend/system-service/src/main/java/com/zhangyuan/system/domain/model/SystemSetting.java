package com.zhangyuan.system.domain.model;

import java.time.Instant;

public class SystemSetting {

    private Long id;
    private String key;
    private String value;
    private Instant createdAt;
    private Instant updatedAt;

    public SystemSetting(String key, String value) {
        this.key = key;
        this.value = value;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateValue(String newValue) {
        this.value = newValue;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
