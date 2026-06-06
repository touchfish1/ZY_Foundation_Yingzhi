package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.service.PaymentChannelStrategy;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MockChannelStrategy implements PaymentChannelStrategy {

    private static final Logger log = LoggerFactory.getLogger(MockChannelStrategy.class);

    @Override
    public String getChannel() {
        return "mock";
    }

    @Override
    public CheckoutResponse createPayment(Payment payment, CheckoutRequest request) {
        String mockPayUrl = "/api/payments/mock/" + payment.getPaymentNo() + "/success";
        log.info("Mock payment created: paymentNo={}, mockPayUrl={}", payment.getPaymentNo(), mockPayUrl);
        return new CheckoutResponse(payment.getPaymentNo(), payment.getStatus(), mockPayUrl, null);
    }

    @Override
    public Payment processCallback(Payment payment, Map<String, String> callbackParams) {
        log.info("Mock callback processed for payment: {}", payment.getPaymentNo());
        payment.markSuccess();
        return payment;
    }

    @Override
    public boolean supportsAsyncCallback() {
        return false;
    }
}
