package com.zhangyuan.modules.payment.domain.repository;

import com.zhangyuan.modules.payment.domain.model.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findByPaymentNo(String paymentNo);

    List<Payment> findAllOrderByCreatedAtDesc();

    Payment save(Payment payment);
}
