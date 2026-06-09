package com.zhangyuan.modules.payment;

import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import com.zhangyuan.modules.order.domain.repository.OrderRepository;
import com.zhangyuan.modules.payment.application.service.PaymentApplicationService;
import com.zhangyuan.modules.payment.domain.model.Payment;
import com.zhangyuan.modules.payment.domain.repository.PaymentRepository;
import com.zhangyuan.modules.payment.domain.service.PaymentDomainService;
import com.zhangyuan.modules.payment.dto.CheckoutRequest;
import com.zhangyuan.modules.payment.dto.CheckoutResponse;
import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final PaymentDomainService paymentDomainService = mock(PaymentDomainService.class);
    private final PaymentApplicationService paymentService = new PaymentApplicationService(
            paymentRepository, paymentDomainService, orderRepository);

    @Test
    void checkoutCreatesPaymentForPendingOrder() {
        CheckoutRequest request = new CheckoutRequest("ORD123", "mock");
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(1L);
        when(order.getOrderNo()).thenReturn(new OrderNumber("ORD123"));
        when(order.getAmount()).thenReturn(BigDecimal.valueOf(29));
        when(order.getCurrency()).thenReturn("CNY");
        when(order.isPending()).thenReturn(true);
        when(orderRepository.findByOrderNo(new OrderNumber("ORD123"))).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutResponse response = paymentService.checkout(request);

        assertThat(response.status()).isEqualTo("pending");
        assertThat(response.mockPayUrl()).contains("/api/payments/mock/");
        assertThat(response.checkoutUrl()).contains("/api/payments/mock/");
        verify(paymentRepository).save(any());
    }

    @Test
    void checkoutThrowsWhenOrderNotFound() {
        CheckoutRequest request = new CheckoutRequest("INVALID", "mock");
        when(orderRepository.findByOrderNo(new OrderNumber("INVALID"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.checkout(request))
                .hasMessageContaining("Order not found");
    }

    @Test
    void checkoutThrowsWhenOrderNotPending() {
        CheckoutRequest request = new CheckoutRequest("ORD123", "mock");
        Order order = mock(Order.class);
        when(order.isPending()).thenReturn(false);
        when(orderRepository.findByOrderNo(new OrderNumber("ORD123"))).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.checkout(request))
                .hasMessageContaining("Order is not payable");
    }

    @Test
    void checkoutThrowsWhenChannelNotMock() {
        CheckoutRequest request = new CheckoutRequest("ORD123", "alipay");

        assertThatThrownBy(() -> paymentService.checkout(request))
                .hasMessageContaining("Only mock payment channel is supported");
    }

    @Test
    void mockSuccessMarksPaymentAndOrderAsPaid() {
        Payment payment = mock(Payment.class);
        when(payment.getPaymentNo()).thenReturn("PAY123");
        when(payment.getOrderId()).thenReturn(1L);
        when(payment.isSuccess()).thenReturn(false);
        when(payment.getStatus()).thenReturn(com.zhangyuan.modules.payment.domain.model.PaymentStatus.SUCCESS);
        when(payment.getPaidAt()).thenReturn(Instant.now());
        when(payment.getChannel()).thenReturn("mock");
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(29));
        when(payment.getCurrency()).thenReturn("CNY");
        when(payment.getCreatedAt()).thenReturn(Instant.now());
        when(paymentRepository.findByPaymentNo("PAY123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);

        Order order = mock(Order.class);
        when(order.getOrderNo()).thenReturn(new OrderNumber("ORD123"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        PaymentResponse response = paymentService.mockSuccess("PAY123");

        assertThat(response.paymentNo()).isEqualTo("PAY123");
        assertThat(response.orderNo()).isEqualTo("ORD123");
        assertThat(response.channel()).isEqualTo("mock");
        verify(payment).markSuccess();
        verify(order).markPaid();
        verify(paymentRepository).save(any());
        verify(orderRepository).save(any());
    }

    @Test
    void mockSuccessIdempotentWhenAlreadyPaid() {
        Payment payment = mock(Payment.class);
        when(payment.getPaymentNo()).thenReturn("PAY123");
        when(payment.getOrderId()).thenReturn(1L);
        when(payment.isSuccess()).thenReturn(true);
        when(payment.getStatus()).thenReturn(com.zhangyuan.modules.payment.domain.model.PaymentStatus.SUCCESS);
        when(payment.getPaidAt()).thenReturn(Instant.now());
        when(payment.getChannel()).thenReturn("mock");
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(29));
        when(payment.getCurrency()).thenReturn("CNY");
        when(payment.getCreatedAt()).thenReturn(Instant.now());
        when(paymentRepository.findByPaymentNo("PAY123")).thenReturn(Optional.of(payment));

        Order order = mock(Order.class);
        when(order.getOrderNo()).thenReturn(new OrderNumber("ORD123"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        PaymentResponse response = paymentService.mockSuccess("PAY123");

        assertThat(response.status()).isEqualTo("success");
        verify(payment, times(1)).isSuccess();
    }

    @Test
    void mockSuccessThrowsWhenPaymentNotFound() {
        when(paymentRepository.findByPaymentNo("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.mockSuccess("INVALID"))
                .hasMessageContaining("Payment not found");
    }

    @Test
    void listPaymentsReturnsAll() {
        Payment payment = mock(Payment.class);
        when(payment.getPaymentNo()).thenReturn("PAY1");
        when(payment.getOrderId()).thenReturn(1L);
        when(payment.getStatus()).thenReturn(com.zhangyuan.modules.payment.domain.model.PaymentStatus.PENDING);
        when(payment.getPaidAt()).thenReturn(null);
        when(payment.getChannel()).thenReturn("mock");
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(29));
        when(payment.getCurrency()).thenReturn("CNY");
        when(payment.getCreatedAt()).thenReturn(Instant.now());
        when(paymentRepository.findPageByCreatedAtDesc(1, 20))
                .thenReturn(PageResponse.of(List.of(payment), 1, 20, 1));

        Order order = mock(Order.class);
        when(order.getOrderNo()).thenReturn(new OrderNumber("ORD1"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = paymentService.listPayments(1, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().paymentNo()).isEqualTo("PAY1");
        assertThat(result.items().getFirst().channel()).isEqualTo("mock");
        assertThat(result.items().getFirst().amount()).isEqualByComparingTo("29");
    }
}
