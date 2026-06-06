package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.client.FulfillmentClient;
import com.zhangyuan.payment.client.OrderServiceClient;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.repository.CompensationEventRepository;
import com.zhangyuan.payment.domain.repository.PaymentRepository;
import com.zhangyuan.payment.domain.service.PaymentDomainService;
import com.zhangyuan.payment.dto.CheckoutRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentEdgeCaseTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final OrderServiceClient orderServiceClient = mock(OrderServiceClient.class);
    private final FulfillmentClient fulfillmentClient = mock(FulfillmentClient.class);
    private final PaymentDomainService domainService = new PaymentDomainService();
    private final ChannelStrategyRegistry strategyRegistry = new ChannelStrategyRegistry(
            java.util.List.of(new MockChannelStrategy()));
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
    void checkout_orderNotFound_throws() {
        when(orderServiceClient.getOrder("NONEXISTENT")).thenReturn(
            new ApiResponse<>(404, "Not found", null));
        assertThatThrownBy(() -> service.checkout(
            new CheckoutRequest("NONEXISTENT", "mock")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkout_emptyOrderNo_throws() {
        assertThatThrownBy(() -> service.checkout(
            new CheckoutRequest("", "mock")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mockSuccess_duplicateCall_stillSucceeds() {
        Payment payment = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");
        payment.markSuccess();
        when(paymentRepository.findByPaymentNo("PAY001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var resp = service.mockSuccess("PAY001");
        assertThat(resp.status()).isIn("SUCCESS", "PENDING");
    }

    @Test
    void paymentDomainService_generatesUniqueNumbers() {
        String no1 = domainService.generatePaymentNo();
        String no2 = domainService.generatePaymentNo();
        assertThat(no1).startsWith("PAY");
        assertThat(no1).isNotEqualTo(no2);
    }
}
