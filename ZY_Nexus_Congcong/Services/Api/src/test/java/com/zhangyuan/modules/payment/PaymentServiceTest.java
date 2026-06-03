package com.zhangyuan.modules.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.modules.order.domain.OrderMain;
import com.zhangyuan.modules.order.repository.OrderMainRepository;
import com.zhangyuan.modules.payment.domain.PaymentTransaction;
import com.zhangyuan.modules.payment.dto.CheckoutRequest;
import com.zhangyuan.modules.payment.dto.CheckoutResponse;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import com.zhangyuan.modules.payment.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private final PaymentTransactionRepository paymentTransactionRepository = mock(PaymentTransactionRepository.class);
    private final OrderMainRepository orderMainRepository = mock(OrderMainRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentService paymentService = new PaymentService(paymentTransactionRepository, orderMainRepository, objectMapper);

    @Test
    void checkoutCreatesPaymentForPendingOrder() {
        CheckoutRequest request = new CheckoutRequest("ORD123", "mock");
        OrderMain order = mock(OrderMain.class);
        when(order.getId()).thenReturn(1L);
        when(order.getOrderNo()).thenReturn("ORD123");
        when(order.getAmount()).thenReturn(BigDecimal.valueOf(29));
        when(order.getCurrency()).thenReturn("CNY");
        when(order.getStatus()).thenReturn("pending");
        when(orderMainRepository.findByOrderNo("ORD123")).thenReturn(Optional.of(order));
        when(paymentTransactionRepository.save(any())).thenAnswer(invocation -> {
            PaymentTransaction saved = invocation.getArgument(0);
            return new PaymentTransaction(saved.getPaymentNo(), saved.getOrderId(), saved.getChannel(),
                    saved.getAmount(), saved.getCurrency(), saved.getRequestJson());
        });

        CheckoutResponse response = paymentService.checkout(request);

        assertThat(response.status()).isEqualTo("pending");
        assertThat(response.mockPayUrl()).contains("/api/payments/mock/");
        assertThat(response.checkoutUrl()).contains("/api/payments/mock/");
        verify(paymentTransactionRepository).save(any());
    }

    @Test
    void checkoutThrowsWhenOrderNotFound() {
        CheckoutRequest request = new CheckoutRequest("INVALID", "mock");
        when(orderMainRepository.findByOrderNo("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.checkout(request))
                .hasMessageContaining("Order not found");
    }

    @Test
    void checkoutThrowsWhenOrderNotPending() {
        CheckoutRequest request = new CheckoutRequest("ORD123", "mock");
        OrderMain order = mock(OrderMain.class);
        when(order.getStatus()).thenReturn("paid");
        when(orderMainRepository.findByOrderNo("ORD123")).thenReturn(Optional.of(order));

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
        PaymentTransaction payment = mock(PaymentTransaction.class);
        when(payment.getPaymentNo()).thenReturn("PAY123");
        when(payment.getOrderId()).thenReturn(1L);
        when(payment.getStatus()).thenReturn("pending");
        when(paymentTransactionRepository.findByPaymentNo("PAY123")).thenReturn(Optional.of(payment));

        OrderMain order = mock(OrderMain.class);
        when(order.getOrderNo()).thenReturn("ORD123");
        when(orderMainRepository.findById(1L)).thenReturn(Optional.of(order));

        PaymentResponse response = paymentService.mockSuccess("PAY123");

        assertThat(response.paymentNo()).isEqualTo("PAY123");
        assertThat(response.orderNo()).isEqualTo("ORD123");
        verify(payment).markPaid(any(), any());
        verify(order).markPaid(any());
    }

    @Test
    void mockSuccessIdempotentWhenAlreadyPaid() {
        PaymentTransaction payment = mock(PaymentTransaction.class);
        when(payment.getPaymentNo()).thenReturn("PAY123");
        when(payment.getOrderId()).thenReturn(1L);
        when(payment.getStatus()).thenReturn("paid");
        when(payment.getPaidAt()).thenReturn(Instant.now());
        when(paymentTransactionRepository.findByPaymentNo("PAY123")).thenReturn(Optional.of(payment));

        OrderMain order = mock(OrderMain.class);
        when(order.getOrderNo()).thenReturn("ORD123");
        when(orderMainRepository.findById(1L)).thenReturn(Optional.of(order));

        PaymentResponse response = paymentService.mockSuccess("PAY123");

        assertThat(response.status()).isEqualTo("paid");
        verify(payment).getStatus();
    }

    @Test
    void mockSuccessThrowsWhenPaymentNotFound() {
        when(paymentTransactionRepository.findByPaymentNo("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.mockSuccess("INVALID"))
                .hasMessageContaining("Payment not found");
    }

    @Test
    void listPaymentsReturnsAll() {
        PaymentTransaction tx = new PaymentTransaction("PAY1", 1L, "mock", BigDecimal.TEN, "CNY", "{}");
        when(paymentTransactionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(tx));
        when(orderMainRepository.findById(1L)).thenReturn(Optional.of(mock(OrderMain.class)));

        List<PaymentResponse> payments = paymentService.listPayments();

        assertThat(payments).hasSize(1);
        assertThat(payments.getFirst().paymentNo()).isEqualTo("PAY1");
    }
}
