package com.zhangyuan.payment.adapter.out.persistence;

import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.repository.PaymentRepository;
import com.zhangyuan.payment.repository.PaymentTransactionJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class JpaPaymentRepository implements PaymentRepository {
    private final PaymentTransactionJpaRepository repo;
    public JpaPaymentRepository(PaymentTransactionJpaRepository repo) { this.repo = repo; }

    @Override
    public Optional<Payment> findByPaymentNo(String paymentNo) {
        return repo.findByPaymentNo(paymentNo).map(this::toDomain);
    }

    @Override
    public Payment save(Payment payment) {
        PaymentTransactionEntity entity;
        if (payment.getId() != null) {
            entity = repo.findById(payment.getId()).orElseThrow();
            entity.setStatus(payment.getStatus());
            entity.setPaidAt(payment.getPaidAt());
            entity.setCallbackJson(payment.getCallbackJson());
        } else {
            entity = new PaymentTransactionEntity();
            entity.setPaymentNo(payment.getPaymentNo());
            entity.setOrderId(payment.getOrderId());
            entity.setOrderNo(payment.getOrderNo());
            entity.setChannel(payment.getChannel());
            entity.setAmount(payment.getAmount());
            entity.setCurrency(payment.getCurrency());
            entity.setRequestJson(payment.getRequestJson());
        }
        return toDomain(repo.save(entity));
    }

    private Payment toDomain(PaymentTransactionEntity entity) {
        Payment p = new Payment();
        p.setId(entity.getId());
        p.setPaymentNo(entity.getPaymentNo());
        p.setOrderId(entity.getOrderId());
        p.setOrderNo(entity.getOrderNo());
        p.setChannel(entity.getChannel());
        p.setAmount(entity.getAmount());
        p.setCurrency(entity.getCurrency());
        p.setStatus(entity.getStatus());
        p.setRequestJson(entity.getRequestJson());
        p.setCallbackJson(entity.getCallbackJson());
        p.setCreatedAt(entity.getCreatedAt());
        p.setPaidAt(entity.getPaidAt());
        return p;
    }
}
