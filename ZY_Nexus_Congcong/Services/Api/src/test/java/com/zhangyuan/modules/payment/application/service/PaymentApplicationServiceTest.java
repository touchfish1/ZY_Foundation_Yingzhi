package com.zhangyuan.modules.payment.application.service;

import com.zhangyuan.modules.payment.domain.model.Payment;
import com.zhangyuan.modules.payment.domain.repository.PaymentRepository;
import com.zhangyuan.modules.payment.domain.service.PaymentDomainService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentApplicationServiceTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final PaymentDomainService paymentDomainService = new PaymentDomainService();
    private final PaymentApplicationService service = new PaymentApplicationService(paymentRepository, paymentDomainService);

    @Test
    void createPaymentSaves() {
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = service.createPayment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");

        assertThat(result.getPaymentNo()).isEqualTo("PAY123");
        assertThat(result.isPending()).isTrue();
        verify(paymentRepository).save(any());
    }

    @Test
    void markSuccessChangesStatus() {
        Payment payment = new Payment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");
        when(paymentRepository.findByPaymentNo("PAY123")).thenReturn(java.util.Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = service.markSuccess("PAY123");

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void listAllDelegates() {
        service.listAll();

        verify(paymentRepository).findAllOrderByCreatedAtDesc();
    }
}
