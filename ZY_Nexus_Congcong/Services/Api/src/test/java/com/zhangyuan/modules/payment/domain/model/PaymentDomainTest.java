package com.zhangyuan.modules.payment.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentDomainTest {

    @Test
    void createPending() {
        Payment p = new Payment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void markSuccess() {
        Payment p = new Payment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");
        p.markSuccess();
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(p.getPaidAt()).isNotNull();
    }

    @Test
    void markSuccessTwiceThrows() {
        Payment p = new Payment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");
        p.markSuccess();
        assertThatThrownBy(p::markSuccess).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markFailed() {
        Payment p = new Payment("PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY");
        p.markFailed();
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}
