package com.zhangyuan.modules.order.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderDomainTest {

    @Test
    void createPendingOrder() {
        Order order = new Order(OrderNumber.generate(), null, 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void markPaid() {
        Order order = new Order(OrderNumber.generate(), null, 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        order.markPaid();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
    }

    @Test
    void markPaidAlreadyPaidThrows() {
        Order order = new Order(OrderNumber.generate(), null, 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        order.markPaid();
        assertThatThrownBy(order::markPaid).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancel() {
        Order order = new Order(OrderNumber.generate(), null, 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelPaidThrows() {
        Order order = new Order(OrderNumber.generate(), null, 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        order.markPaid();
        assertThatThrownBy(order::cancel).isInstanceOf(IllegalStateException.class);
    }
}
