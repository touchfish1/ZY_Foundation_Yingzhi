package com.zhangyuan.order.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    void createOrder() {
        Order order = new Order(new OrderNumber("ORD123"), 1L, 1L, BigDecimal.valueOf(99), "CNY", "{}");
        order.setUserId(100L);

        assertThat(order.getOrderNo().value()).isEqualTo("ORD123");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getAmount()).isEqualByComparingTo("99");
        assertThat(order.getUserId()).isEqualTo(100L);
    }

    @Test
    void markPaid() {
        Order order = new Order(new OrderNumber("ORD123"), 1L, 1L, BigDecimal.TEN, "CNY", "{}");
        order.markPaid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
    }

    @Test
    void markPaid_whenCancelled_throws() {
        Order order = new Order(new OrderNumber("ORD123"), 1L, 1L, BigDecimal.TEN, "CNY", "{}");
        order.cancel();

        assertThatThrownBy(order::markPaid).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancel() {
        Order order = new Order(new OrderNumber("ORD123"), 1L, 1L, BigDecimal.TEN, "CNY", "{}");
        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancel_whenPaid_throws() {
        Order order = new Order(new OrderNumber("ORD123"), 1L, 1L, BigDecimal.TEN, "CNY", "{}");
        order.markPaid();

        assertThatThrownBy(order::cancel).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void orderNumber_generatesUniqueValue() {
        OrderNumber n1 = OrderNumber.generate();
        OrderNumber n2 = OrderNumber.generate();

        assertThat(n1.value()).startsWith("ORD");
        assertThat(n1.value()).isNotEqualTo(n2.value());
    }

    @Test
    void orderNumber_rejectsBlank() {
        assertThatThrownBy(() -> new OrderNumber(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrderNumber(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrderNumber("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void statusTransitions() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.EXPIRED)).isTrue();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.REFUNDED)).isTrue();

        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(OrderStatus.EXPIRED.canTransitionTo(OrderStatus.PAID)).isFalse();
        assertThat(OrderStatus.REFUNDED.canTransitionTo(OrderStatus.PENDING)).isFalse();
    }

    @Test
    void isPaid_returnsTrue_whenPaid() {
        Order order = new Order(new OrderNumber("ORD123"), 1L, 1L, BigDecimal.TEN, "CNY", "{}");
        order.markPaid();
        assertThat(order.isPaid()).isTrue();
    }

    @Test
    void isPending_returnsTrue_whenPending() {
        Order order = new Order(new OrderNumber("ORD123"), 1L, 1L, BigDecimal.TEN, "CNY", "{}");
        assertThat(order.isPending()).isTrue();
    }

    @Test
    void markFulfilled_transitionsFromPaid() {
        Order order = new Order(new OrderNumber("ORD123"), 1L, 1L, BigDecimal.TEN, "CNY", "{}");
        order.markPaid();
        order.markFulfilled();
        assertThat(order.isFulfilled()).isTrue();
        assertThat(order.getFulfilledAt()).isNotNull();
    }

    @Test
    void markFulfilled_fromPending_throws() {
        Order order = new Order(new OrderNumber("ORD123"), 1L, 1L, BigDecimal.TEN, "CNY", "{}");
        assertThatThrownBy(order::markFulfilled)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fulfilledStatus_canTransitionToRefunded() {
        assertThat(OrderStatus.FULFILLED.canTransitionTo(OrderStatus.REFUNDED)).isTrue();
        assertThat(OrderStatus.FULFILLED.canTransitionTo(OrderStatus.PAID)).isFalse();
        assertThat(OrderStatus.FULFILLED.canTransitionTo(OrderStatus.PENDING)).isFalse();
    }

    @Test
    void paidStatus_canTransitionToFulfilled() {
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.FULFILLED)).isTrue();
    }
}
