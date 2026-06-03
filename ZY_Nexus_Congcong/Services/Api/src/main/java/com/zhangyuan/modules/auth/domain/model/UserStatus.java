package com.zhangyuan.modules.auth.domain.model;

public enum UserStatus {
    ENABLED,
    DISABLED;

    public boolean isActive() {
        return this == ENABLED;
    }
}
