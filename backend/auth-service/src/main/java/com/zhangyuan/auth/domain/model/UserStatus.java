package com.zhangyuan.auth.domain.model;

public enum UserStatus {
    ENABLED,
    DISABLED;

    public boolean isActive() {
        return this == ENABLED;
    }
}
