package com.zhangyuan.auth.domain.model;

import java.time.Instant;

public class Role {

    private Long id;
    private String code;
    private String name;
    private Instant createdAt;

    public Role(String code, String name) {
        this.code = code;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public void rename(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}
