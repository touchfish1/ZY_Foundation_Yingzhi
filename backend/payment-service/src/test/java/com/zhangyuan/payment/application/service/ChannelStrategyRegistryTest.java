package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.service.PaymentChannelStrategy;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ChannelStrategyRegistryTest {

    @Test
    void registry_loadsAllStrategies() {
        var mockStrategy = new MockChannelStrategy();
        var registry = new ChannelStrategyRegistry(List.of(mockStrategy));

        assertThat(registry.hasStrategy("mock")).isTrue();
        assertThat(registry.hasStrategy("alipay")).isFalse();
        assertThat(registry.getAll()).hasSize(1);
    }

    @Test
    void registry_getUnknownStrategy_throws() {
        var registry = new ChannelStrategyRegistry(List.of());

        assertThatThrownBy(() -> registry.getStrategy("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported payment channel");
    }

    @Test
    void mockStrategy_createPayment_returnsExpectedResponse() {
        var strategy = new MockChannelStrategy();
        Payment payment = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");
        CheckoutResponse resp = strategy.createPayment(payment, new CheckoutRequest("ORD001", "mock"));

        assertThat(resp.paymentNo()).isEqualTo("PAY001");
        assertThat(resp.payUrl()).contains("/api/payments/mock/PAY001/success");
        assertThat(resp.checkoutUrl()).isNull();
    }

    @Test
    void mockStrategy_processCallback_marksSuccess() {
        var strategy = new MockChannelStrategy();
        Payment payment = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");

        strategy.processCallback(payment, Map.of());

        assertThat(payment.getStatus()).isEqualTo("SUCCESS");
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    void mockStrategy_doesNotSupportAsyncCallback() {
        var strategy = new MockChannelStrategy();
        assertThat(strategy.supportsAsyncCallback()).isFalse();
    }

    @Test
    void mockStrategy_getChannel_returnsMock() {
        var strategy = new MockChannelStrategy();
        assertThat(strategy.getChannel()).isEqualTo("mock");
    }

    @Test
    void payment_stateTransitions_successOnlyFromPending() {
        Payment payment = new Payment("PAY001", 1L, "ORD001", "mock", BigDecimal.TEN, "CNY");

        assertThat(payment.getStatus()).isEqualTo("PENDING");
        payment.markSuccess();
        assertThat(payment.getStatus()).isEqualTo("SUCCESS");

        // markSuccess on already SUCCESS should be idempotent
        payment.markSuccess();
        assertThat(payment.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void payment_markFailed_transitionsCorrectly() {
        Payment payment = new Payment("PAY002", 1L, "ORD002", "alipay", BigDecimal.valueOf(99), "CNY");
        payment.markFailed();
        assertThat(payment.getStatus()).isEqualTo("FAILED");
    }
}
