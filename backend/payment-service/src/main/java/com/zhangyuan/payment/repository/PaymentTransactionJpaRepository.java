package com.zhangyuan.payment.repository;

import com.zhangyuan.payment.adapter.out.persistence.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentTransactionJpaRepository extends JpaRepository<PaymentTransactionEntity, Long> {
    Optional<PaymentTransactionEntity> findByPaymentNo(String paymentNo);
}
