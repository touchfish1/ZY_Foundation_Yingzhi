package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.client.FulfillmentClient;
import com.zhangyuan.payment.client.OrderServiceClient;
import com.zhangyuan.payment.common.ApiResponse;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.repository.CompensationEventRepository;
import com.zhangyuan.payment.domain.repository.PaymentRepository;
import com.zhangyuan.payment.domain.service.PaymentDomainService;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import com.zhangyuan.payment.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentApplicationServiceTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final OrderServiceClient orderServiceClient = mock(OrderServiceClient.class);
    private final FulfillmentClient fulfillmentClient = mock(FulfillmentClient.class);
    private final PaymentDomainService domainService = new PaymentDomainService();
    private final ChannelStrategyRegistry strategyRegistry = new ChannelStrategyRegistry(
            List.of(new MockChannelStrategy()));
    private final CompensationEventRepository compensationEventRepo = mock(CompensationEventRepository.class);
    private final CompensationService compensationService = new CompensationService(
            compensationEventRepo, fulfillmentClient, null);
    private PaymentApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PaymentApplicationService(paymentRepository, orderServiceClient,
                fulfillmentClient, domainService, strategyRegistry, compensationService);
    }

    @Test
    void checkout_createsMockPayment() {
        doReturn(ApiResponse.ok(new Object())).when(orderServiceClient).getOrder("ORD001");
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CheckoutResponse resp = service.checkout(new CheckoutRequest("ORD001", "mock"));

        assertThat(resp.paymentNo()).startsWith("PAY");
        assertThat(resp.status()).isEqualTo("PENDING");
        assertThat(resp.mockPayUrl()).contains("/api/payments/mock/");
    }

    @Test
    void checkout_orderNotFound_throws() {
        doReturn(ApiResponse.error(404, "Order not found")).when(orderServiceClient).getOrder("ORD_NOT_FOUND");

        assertThatThrownBy(() -> service.checkout(new CheckoutRequest("ORD_NOT_FOUND", "mock")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void checkout_unsupportedChannel_throws() {
        doReturn(ApiResponse.ok(new Object())).when(orderServiceClient).getOrder("ORD001");

        assertThatThrownBy(() -> service.checkout(new CheckoutRequest("ORD001", "alipay")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported channel");
    }

    @Test
    void mockSuccess_marksPaymentAsSuccess() {
        Payment payment = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");
        when(paymentRepository.findByPaymentNo("PAY001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(fulfillmentClient.fulfillOrder("ORD001")).thenReturn(ApiResponse.ok());

        CheckoutResponse resp = service.mockSuccess("PAY001");

        assertThat(resp.status()).isEqualTo("SUCCESS");
        verify(fulfillmentClient).fulfillOrder("ORD001");
    }

    @Test
    void mockSuccess_paymentNotFound_throws() {
        when(paymentRepository.findByPaymentNo("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.mockSuccess("NONEXISTENT"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mockSuccess_fulfillmentFailure_loggedAndContinues() {
        Payment payment = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");
        when(paymentRepository.findByPaymentNo("PAY001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(fulfillmentClient.fulfillOrder("ORD001")).thenThrow(new RuntimeException("Fulfillment service down"));

        // Should not propagate the exception
        CheckoutResponse resp = service.mockSuccess("PAY001");

        assertThat(resp.status()).isEqualTo("SUCCESS");
        assertThat(resp.paymentNo()).isEqualTo("PAY001");
        verify(fulfillmentClient).fulfillOrder("ORD001");
    }

    @Test
    void getPayment_returnsPaymentResponse() {
        Payment payment = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");
        payment.markSuccess();
        when(paymentRepository.findByPaymentNo("PAY001")).thenReturn(Optional.of(payment));

        PaymentResponse resp = service.getPayment("PAY001");

        assertThat(resp.paymentNo()).isEqualTo("PAY001");
        assertThat(resp.orderNo()).isEqualTo("ORD001");
        assertThat(resp.status()).isEqualTo("SUCCESS");
        assertThat(resp.paidAt()).isNotNull();
    }

    @Test
    void getPayment_notFound_throws() {
        when(paymentRepository.findByPaymentNo("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPayment("NONEXISTENT"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
