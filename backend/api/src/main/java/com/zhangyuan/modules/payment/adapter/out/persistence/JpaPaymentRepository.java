package com.zhangyuan.modules.payment.adapter.out.persistence;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.payment.domain.model.Payment;
import com.zhangyuan.modules.payment.domain.model.PaymentStatus;
import com.zhangyuan.modules.payment.domain.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaPaymentRepository implements PaymentRepository {

    private final PaymentTransactionJpaRepository jpaRepository;

    public JpaPaymentRepository(PaymentTransactionJpaRepository jpaRepository) {
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
    public PageResponse<Payment> findPageByCreatedAtDesc(int page, int pageSize) {
        Page<PaymentTransactionEntity> pageResult = jpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, pageSize));
        List<Payment> items = pageResult.getContent().stream()
                .map(this::toDomain)
                .toList();
        return PageResponse.of(items, page, pageSize, pageResult.getTotalElements());
    }

    @Override
    public Payment save(Payment payment) {
        PaymentTransactionEntity entity = new PaymentTransactionEntity(
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

    private Payment toDomain(PaymentTransactionEntity entity) {
        Payment payment = new Payment(
                entity.getPaymentNo(),
                entity.getOrderId(),
                entity.getChannel(),
                entity.getAmount(),
                entity.getCurrency()
        );
        if ("paid".equals(entity.getStatus())) {
            payment.markSuccess();
        }
        return payment;
    }
}
