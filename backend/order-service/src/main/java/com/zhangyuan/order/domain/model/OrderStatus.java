package com.zhangyuan.order.domain.model;

public enum OrderStatus {
    PENDING,
    PAID,
    FULFILLED,
    CANCELLED,
    EXPIRED,
    REFUNDED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == PAID || target == CANCELLED || target == EXPIRED;
            case PAID -> target == FULFILLED || target == REFUNDED;
            case FULFILLED -> target == REFUNDED;
            case CANCELLED, EXPIRED, REFUNDED -> false;
        };
    }
}
