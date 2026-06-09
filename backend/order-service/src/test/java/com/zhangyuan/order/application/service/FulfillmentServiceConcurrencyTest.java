package com.zhangyuan.order.application.service;

import com.zhangyuan.order.client.UserServiceClient;
import com.zhangyuan.order.domain.model.Order;
import com.zhangyuan.order.domain.model.OrderNumber;
import com.zhangyuan.order.domain.model.UserSubscription;
import com.zhangyuan.order.domain.repository.OrderRepository;
import com.zhangyuan.order.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FulfillmentServiceConcurrencyTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
    private final UserServiceClient userServiceClient = mock(UserServiceClient.class);
    private final FulfillmentService service = new FulfillmentService(orderRepository, subscriptionRepository, userServiceClient);

    @Test
    void sequentialDuplicateFulfill_shouldBeIdempotent() {
        Order order = createPaidOrder();
        when(orderRepository.findByOrderNo(new OrderNumber("ORD001"))).thenReturn(Optional.of(order));
        when(subscriptionRepository.findByUserIdAndActive(1L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.fulfillOrder("ORD001");
        service.fulfillOrder("ORD001");

        verify(subscriptionRepository, times(1)).save(any());
    }

    @Test
    void fulfill_shouldNotCreateSubscription_whenOrderNotPaid() {
        Order order = new Order(new OrderNumber("ORD001"), null, 1L, 1L, BigDecimal.valueOf(99), "CNY", "{}");
        when(orderRepository.findByOrderNo(new OrderNumber("ORD001"))).thenReturn(Optional.of(order));

        service.fulfillOrder("ORD001");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void fulfill_shouldNotCreateSubscription_whenAlreadyFulfilled() {
        Order order = createPaidOrder();
        order.markFulfilled();
        when(orderRepository.findByOrderNo(new OrderNumber("ORD001"))).thenReturn(Optional.of(order));

        service.fulfillOrder("ORD001");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void fulfill_whenSubscriptionAlreadyExists_shouldExtendRatherThanCreate() {
        Order order = createPaidOrder();
        UserSubscription existing = new UserSubscription(1L, "basic", "Basic Plan", 30);
        when(orderRepository.findByOrderNo(new OrderNumber("ORD001"))).thenReturn(Optional.of(order));
        when(subscriptionRepository.findByUserIdAndActive(1L)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.fulfillOrder("ORD001");

        verify(subscriptionRepository).save(argThat(sub ->
                sub.getUserId() == 1L && sub.getPlanCode().equals("basic")
        ));
    }

    private Order createPaidOrder() {
        Order o = new Order(new OrderNumber("ORD001"), 1L, 1L, 1L, BigDecimal.valueOf(99), "CNY",
                "{\"planCode\":\"basic\",\"planName\":\"Basic Plan\",\"validityDays\":\"30\"}");
        o.markPaid();
        return o;
    }
}
