package com.zhangyuan.modules.payment.adapter.out.persistence;

import com.zhangyuan.modules.payment.domain.model.Payment;
import com.zhangyuan.modules.payment.domain.model.PaymentStatus;
import com.zhangyuan.modules.payment.domain.repository.PaymentRepository;
import com.zhangyuan.modules.payment.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaPaymentRepository implements PaymentRepository {

    private final PaymentTransactionRepository jpaRepository;

    public JpaPaymentRepository(PaymentTransactionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Payment> findByPaymentNo(String paymentNo) {
        return jpaRepository.findByPaymentNo(paymentNo).map(this::toDomain);
    }

    @Override
    public List<Payment> findAllOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Payment save(Payment payment) {
        com.zhangyuan.modules.payment.domain.PaymentTransaction entity = new com.zhangyuan.modules.payment.domain.PaymentTransaction(
                payment.getPaymentNo(),
                payment.getOrderId(),
                payment.getChannel(),
                payment.getAmount(),
                payment.getCurrency(),
                "{}"
        );
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private Payment toDomain(com.zhangyuan.modules.payment.domain.PaymentTransaction entity) {
        Payment payment = new Payment(
                entity.getPaymentNo(),
                entity.getOrderId(),
                entity.getChannel(),
                entity.getAmount(),
                entity.getCurrency()
        );
        if ("success".equals(entity.getStatus())) {
            payment.markSuccess();
        }
        return payment;
    }
}
