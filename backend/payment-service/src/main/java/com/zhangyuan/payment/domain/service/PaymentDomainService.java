package com.zhangyuan.payment.domain.service;

import com.zhangyuan.payment.domain.model.Payment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentDomainService {

    public String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    public Payment createPayment(String orderNo, String channel, BigDecimal amount, String currency) {
        String paymentNo = generatePaymentNo();
        return new Payment(paymentNo, 0L, orderNo, channel, amount, currency);
    }
}
