package com.zhangyuan.modules.payment.application.service;

import com.zhangyuan.modules.payment.domain.model.Payment;
import com.zhangyuan.modules.payment.domain.repository.PaymentRepository;
import com.zhangyuan.modules.payment.domain.service.PaymentDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 支付应用服务，负责支付创建和状态更新的编排。
 */
@Service
public class PaymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentDomainService paymentDomainService;

    public PaymentApplicationService(PaymentRepository paymentRepository, PaymentDomainService paymentDomainService) {
        this.paymentRepository = paymentRepository;
        this.paymentDomainService = paymentDomainService;
    }

    /**
     * 创建支付记录。
     *
     * @param paymentNo 支付单号
     * @param orderId   订单 ID
     * @param channel   支付通道
     * @param amount    支付金额
     * @param currency  货币类型
     * @return 创建的支付记录
     */
    @Transactional
    public Payment createPayment(String paymentNo, Long orderId, String channel, BigDecimal amount, String currency) {
        log.info("Creating payment: paymentNo={}, orderId={}, channel={}, amount={}", paymentNo, orderId, channel, amount);
        Payment payment = new Payment(paymentNo, orderId, channel, amount, currency);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment created: paymentNo={}", saved.getPaymentNo());
        return saved;
    }

    /**
     * 将指定支付标记为成功。
     *
     * @param paymentNo 支付单号
     * @return 更新后的支付记录
     */
    @Transactional
    public Payment markSuccess(String paymentNo) {
        log.info("Marking payment as success: {}", paymentNo);
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> {
                    log.error("Payment not found: {}", paymentNo);
                    return new IllegalArgumentException("Payment not found: " + paymentNo);
                });
        paymentDomainService.validateCheckout(payment);
        payment.markSuccess();
        Payment saved = paymentRepository.save(payment);
        log.info("Payment marked as success: {}", paymentNo);
        return saved;
    }

    /**
     * 根据支付单号查询支付记录。
     *
     * @param paymentNo 支付单号
     * @return 支付记录 Optional
     */
    @Transactional(readOnly = true)
    public Optional<Payment> findByPaymentNo(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo);
    }

    /**
     * 获取所有支付记录列表。
     *
     * @return 支付记录列表
     */
    @Transactional(readOnly = true)
    public List<Payment> listAll() {
        return paymentRepository.findAllOrderByCreatedAtDesc();
    }
}
