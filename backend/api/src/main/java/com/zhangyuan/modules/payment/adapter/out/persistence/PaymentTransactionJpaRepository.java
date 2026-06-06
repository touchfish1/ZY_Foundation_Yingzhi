package com.zhangyuan.modules.payment.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionJpaRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    Optional<PaymentTransactionEntity> findByPaymentNo(String paymentNo);

    List<PaymentTransactionEntity> findAllByOrderByCreatedAtDesc();

    Page<PaymentTransactionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
