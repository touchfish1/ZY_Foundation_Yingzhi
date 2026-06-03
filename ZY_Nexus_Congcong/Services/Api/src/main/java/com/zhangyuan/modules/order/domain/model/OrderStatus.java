package com.zhangyuan.modules.order.domain.model;

public enum OrderStatus {
    PENDING,
    PAID,
    CANCELLED,
    EXPIRED,
    REFUNDED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == PAID || target == CANCELLED || target == EXPIRED;
            case PAID -> target == REFUNDED;
            case CANCELLED, EXPIRED, REFUNDED -> false;
        };
    }
}
