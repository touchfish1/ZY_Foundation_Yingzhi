package com.zhangyuan.payment.domain.service;

import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;

import java.util.Map;

public interface PaymentChannelStrategy {

    String getChannel();

    CheckoutResponse createPayment(Payment payment, CheckoutRequest request);

    Payment processCallback(Payment payment, Map<String, String> callbackParams);

    default boolean supportsAsyncCallback() {
        return false;
    }
}
