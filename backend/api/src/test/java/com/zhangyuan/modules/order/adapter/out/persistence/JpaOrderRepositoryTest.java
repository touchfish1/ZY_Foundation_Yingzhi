package com.zhangyuan.modules.order.adapter.out.persistence;

import com.zhangyuan.modules.order.adapter.out.persistence.OrderMainEntity;
import com.zhangyuan.modules.order.adapter.out.persistence.OrderMainEntityRepository;
import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaOrderRepositoryTest {

    private final OrderMainEntityRepository jpaRepository = mock(OrderMainEntityRepository.class);
    private final JpaOrderRepository repository = new JpaOrderRepository(jpaRepository);

    @Test
    void saveNewOrder() {
        Order order = new Order(OrderNumber.generate(), null, 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        OrderMainEntity entity = new OrderMainEntity(
                order.getOrderNo().value(), 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        when(jpaRepository.save(any())).thenReturn(entity);

        Order result = repository.save(order);

        assertThat(result.getOrderNo().value()).isEqualTo(order.getOrderNo().value());
        verify(jpaRepository).save(any());
    }

    @Test
    void findByOrderNoDelegates() {
        OrderMainEntity entity = new OrderMainEntity(
                "ORD123", 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        when(jpaRepository.findByOrderNo("ORD123")).thenReturn(Optional.of(entity));

        Optional<Order> result = repository.findByOrderNo(new OrderNumber("ORD123"));

        assertThat(result).isPresent();
        assertThat(result.get().getOrderNo().value()).isEqualTo("ORD123");
    }
}
