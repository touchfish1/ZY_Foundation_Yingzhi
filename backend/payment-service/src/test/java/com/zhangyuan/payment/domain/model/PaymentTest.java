package com.zhangyuan.payment.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Test
    void createPayment() {
        Payment p = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");
        assertThat(p.getPaymentNo()).isEqualTo("PAY001");
        assertThat(p.getOrderNo()).isEqualTo("ORD001");
        assertThat(p.getOrderId()).isEqualTo(1L);
        assertThat(p.getChannel()).isEqualTo("mock");
        assertThat(p.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(p.getCurrency()).isEqualTo("CNY");
        assertThat(p.getStatus()).isEqualTo("PENDING");
        assertThat(p.getCreatedAt()).isNotNull();
    }

    @Test
    void markSuccess() {
        Payment p = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");
        p.markSuccess();
        assertThat(p.getStatus()).isEqualTo("SUCCESS");
        assertThat(p.getPaidAt()).isNotNull();
    }

    @Test
    void markFailed() {
        Payment p = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");
        p.markFailed();
        assertThat(p.getStatus()).isEqualTo("FAILED");
    }

    @Test
    void statusEnum_containsAllExpectedValues() {
        assertThat(PaymentStatus.values())
                .containsExactlyInAnyOrder(
                        PaymentStatus.PENDING,
                        PaymentStatus.PROCESSING,
                        PaymentStatus.SUCCESS,
                        PaymentStatus.FAILED,
                        PaymentStatus.CLOSED,
                        PaymentStatus.REFUNDED
                );
    }
}
