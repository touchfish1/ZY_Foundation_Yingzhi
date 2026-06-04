package com.zhangyuan.modules.order.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void pendingCanTransitionToPaid() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PAID)).isTrue();
    }

    @Test
    void pendingCanTransitionToCancelled() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void paidCannotTransitionToPending() {
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PENDING)).isFalse();
    }

    @Test
    void paidCanTransitionToRefunded() {
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.REFUNDED)).isTrue();
    }

    @Test
    void cancelledCannotTransition() {
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PAID)).isFalse();
    }
}
