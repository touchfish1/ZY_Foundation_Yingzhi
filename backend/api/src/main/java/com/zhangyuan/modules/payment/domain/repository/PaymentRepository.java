package com.zhangyuan.modules.payment.domain.repository;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.payment.domain.model.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findByPaymentNo(String paymentNo);

    List<Payment> findAllOrderByCreatedAtDesc();

    PageResponse<Payment> findPageByCreatedAtDesc(int page, int pageSize);

    Payment save(Payment payment);
}
