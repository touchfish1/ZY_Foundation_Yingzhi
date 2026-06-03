package com.zhangyuan.modules.order.application.service;

import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import com.zhangyuan.modules.order.domain.repository.OrderRepository;
import com.zhangyuan.modules.order.domain.service.OrderDomainService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderApplicationServiceTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderDomainService orderDomainService = new OrderDomainService();
    private final OrderApplicationService service = new OrderApplicationService(orderRepository, orderDomainService);

    @Test
    void createOrderSavesAndReturns() {
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = service.createOrder(1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");

        assertThat(result.getPlanId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(29));
        assertThat(result.isPending()).isTrue();
        verify(orderRepository).save(any());
    }

    @Test
    void findByOrderNoDelegates() {
        OrderNumber orderNo = OrderNumber.generate();
        Order order = new Order(orderNo, 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        when(orderRepository.findByOrderNo(orderNo)).thenReturn(Optional.of(order));

        Optional<Order> result = service.findByOrderNo(orderNo.value());

        assertThat(result).isPresent();
        assertThat(result.get().getOrderNo()).isEqualTo(orderNo);
    }

    @Test
    void listAllDelegates() {
        when(orderRepository.findAllOrderByCreatedAtDesc()).thenReturn(java.util.List.of());

        var result = service.listAll();

        assertThat(result).isNotNull();
        verify(orderRepository).findAllOrderByCreatedAtDesc();
    }

    @Test
    void markPaidChangesStatus() {
        Order order = new Order(OrderNumber.generate(), 1L, 1L, BigDecimal.valueOf(29), "CNY", "{}");
        when(orderRepository.findByOrderNo(any())).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = service.markPaid(order.getOrderNo().value());

        assertThat(result.isPaid()).isTrue();
        verify(orderRepository).save(any());
    }
}
