package com.zhangyuan.order.application.service;

import com.zhangyuan.order.domain.model.Order;
import com.zhangyuan.order.domain.model.OrderNumber;
import com.zhangyuan.order.domain.model.UserSubscription;
import com.zhangyuan.order.domain.repository.OrderRepository;
import com.zhangyuan.order.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FulfillmentServiceTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
    private FulfillmentService service;

    @BeforeEach
    void setUp() {
        service = new FulfillmentService(orderRepository, subscriptionRepository);
    }

    private Order createPaidOrder() {
        Order o = new Order(new OrderNumber("ORD001"), 1L, 1L, BigDecimal.valueOf(99), "CNY",
                "{\"planCode\":\"premium\",\"planName\":\"Premium Plan\",\"validityDays\":\"30\"}");
        o.setUserId(1L);
        o.markPaid();
        return o;
    }

    @Test
    void fulfillOrder_createsNewSubscription() {
        Order order = createPaidOrder();
        when(orderRepository.findByOrderNo(new OrderNumber("ORD001"))).thenReturn(Optional.of(order));
        when(subscriptionRepository.findByUserIdAndActive(1L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.fulfillOrder("ORD001");

        verify(subscriptionRepository).save(argThat(sub ->
                sub.getUserId() == 1L && sub.getPlanCode().equals("premium") && sub.getStatus().equals("active")
        ));
    }

    @Test
    void fulfillOrder_extendsExistingSubscription() {
        Order order = createPaidOrder();
        UserSubscription existing = new UserSubscription(1L, "premium", "Premium Plan", 30);
        when(orderRepository.findByOrderNo(new OrderNumber("ORD001"))).thenReturn(Optional.of(order));
        when(subscriptionRepository.findByUserIdAndActive(1L)).thenReturn(Optional.of(existing));

        service.fulfillOrder("ORD001");

        verify(subscriptionRepository).save(any());
    }

    @Test
    void fulfillOrder_whenNotPaid_doesNothing() {
        Order order = new Order(new OrderNumber("ORD001"), 1L, 1L, BigDecimal.valueOf(99), "CNY", "{}");
        when(orderRepository.findByOrderNo(new OrderNumber("ORD001"))).thenReturn(Optional.of(order));

        service.fulfillOrder("ORD001");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void fulfillOrder_whenOrderNotFound_throws() {
        when(orderRepository.findByOrderNo(new OrderNumber("MISSING"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.fulfillOrder("MISSING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");

        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void fulfillOrder_usesDefaultValidityDays_whenSnapshotMalformed() {
        Order order = new Order(new OrderNumber("ORD001"), 1L, 1L, BigDecimal.valueOf(99), "CNY",
                "{\"planCode\":\"premium\",\"planName\":\"Premium Plan\",\"validityDays\":\"invalid\"}");
        order.setUserId(1L);
        order.markPaid();

        when(orderRepository.findByOrderNo(new OrderNumber("ORD001"))).thenReturn(Optional.of(order));
        when(subscriptionRepository.findByUserIdAndActive(1L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.fulfillOrder("ORD001");

        verify(subscriptionRepository).save(argThat(sub ->
                sub.getUserId() == 1L && sub.getPlanCode().equals("premium")
        ));
    }
}
