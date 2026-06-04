package com.zhangyuan.modules.order.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void createPendingOrder() {
        Order order = new Order(OrderNumber.generate(), 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.isPending()).isTrue();
        assertThat(order.isPaid()).isFalse();
    }

    @Test
    void markPaidChangesStatus() {
        Order order = new Order(OrderNumber.generate(), 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        order.markPaid();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.isPaid()).isTrue();
        assertThat(order.getPaidAt()).isNotNull();
    }

    @Test
    void markPaidOnAlreadyPaidThrows() {
        Order order = new Order(OrderNumber.generate(), 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        order.markPaid();
        assertThatThrownBy(order::markPaid).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelPendingOrder() {
        Order order = new Order(OrderNumber.generate(), 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelPaidOrderThrows() {
        Order order = new Order(OrderNumber.generate(), 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        order.markPaid();
        assertThatThrownBy(order::cancel).isInstanceOf(IllegalStateException.class);
    }
}
