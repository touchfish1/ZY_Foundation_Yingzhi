package com.zhangyuan.modules.payment.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Test
    void createPendingPayment() {
        Payment payment = new Payment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.isPending()).isTrue();
    }

    @Test
    void markSuccessChangesStatus() {
        Payment payment = new Payment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");
        payment.markSuccess();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.isSuccess()).isTrue();
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    void markSuccessOnAlreadySuccessThrows() {
        Payment payment = new Payment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");
        payment.markSuccess();
        assertThatThrownBy(payment::markSuccess).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markFailedChangesStatus() {
        Payment payment = new Payment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");
        payment.markFailed();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}
