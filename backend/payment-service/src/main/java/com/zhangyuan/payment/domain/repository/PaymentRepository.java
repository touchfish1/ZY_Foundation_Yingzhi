package com.zhangyuan.payment.domain.repository;

import com.zhangyuan.payment.domain.model.Payment;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findByPaymentNo(String paymentNo);
    Payment save(Payment payment);
}
