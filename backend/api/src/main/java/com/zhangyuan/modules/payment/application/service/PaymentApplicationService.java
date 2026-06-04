package com.zhangyuan.modules.payment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.modules.order.adapter.out.persistence.OrderMainEntity;
import com.zhangyuan.modules.order.adapter.out.persistence.OrderMainEntityRepository;
import com.zhangyuan.modules.payment.adapter.out.persistence.PaymentTransactionEntity;
import com.zhangyuan.modules.payment.adapter.out.persistence.PaymentTransactionJpaRepository;
import com.zhangyuan.modules.payment.domain.model.Payment;
import com.zhangyuan.modules.payment.domain.repository.PaymentRepository;
import com.zhangyuan.modules.payment.domain.service.PaymentDomainService;
import com.zhangyuan.modules.payment.dto.CheckoutRequest;
import com.zhangyuan.modules.payment.dto.CheckoutResponse;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentDomainService paymentDomainService;
    private final PaymentTransactionJpaRepository paymentTransactionJpaRepository;
    private final OrderMainEntityRepository orderMainRepository;
    private final ObjectMapper objectMapper;

    public PaymentApplicationService(PaymentRepository paymentRepository,
                                     PaymentDomainService paymentDomainService,
                                     PaymentTransactionJpaRepository paymentTransactionJpaRepository,
                                     OrderMainEntityRepository orderMainRepository,
                                     ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentDomainService = paymentDomainService;
        this.paymentTransactionJpaRepository = paymentTransactionJpaRepository;
        this.orderMainRepository = orderMainRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Payment createPayment(String paymentNo, Long orderId, String channel, BigDecimal amount, String currency) {
        log.info("Creating payment: paymentNo={}, orderId={}, channel={}, amount={}", paymentNo, orderId, channel, amount);
        Payment payment = new Payment(paymentNo, orderId, channel, amount, currency);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment created: paymentNo={}", saved.getPaymentNo());
        return saved;
    }

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

    @Transactional(readOnly = true)
    public Optional<Payment> findByPaymentNo(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo);
    }

    @Transactional(readOnly = true)
    public List<Payment> listAll() {
        return paymentRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 发起支付下单（仅支持 Mock 模拟支付）。
     */
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        log.info("Checkout: orderNo={}, channel={}", request.orderNo(), request.channel());
        if (!"mock".equalsIgnoreCase(request.channel().trim())) {
            log.warn("Only mock payment channel is supported, got: {}", request.channel());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only mock payment channel is supported");
        }
        OrderMainEntity order = orderMainRepository.findByOrderNo(request.orderNo().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!"pending".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not payable");
        }

        PaymentTransactionEntity payment = new PaymentTransactionEntity(
                nextPaymentNo(),
                order.getId(),
                "mock",
                order.getAmount(),
                order.getCurrency(),
                json(Map.of("orderNo", order.getOrderNo(), "channel", "mock", "createdAt", Instant.now().toString()))
        );
        payment = paymentTransactionJpaRepository.save(payment);
        String mockPayUrl = "/api/payments/mock/" + payment.getPaymentNo() + "/success";
        return new CheckoutResponse(payment.getPaymentNo(), payment.getStatus(), mockPayUrl, mockPayUrl);
    }

    /**
     * 模拟支付成功，更新支付和订单状态。
     */
    @Transactional
    public PaymentResponse mockSuccess(String paymentNo) {
        log.info("Mock payment success: {}", paymentNo);
        PaymentTransactionEntity payment = paymentTransactionJpaRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        OrderMainEntity order = orderMainRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!"paid".equals(payment.getStatus())) {
            Instant now = Instant.now();
            payment.markPaid(json(Map.of("mockResult", "success", "paidAt", now.toString())), now);
            order.markPaid(now);
        }
        return new PaymentResponse(payment.getPaymentNo(), order.getOrderNo(), payment.getStatus(), payment.getPaidAt());
    }

    /**
     * 获取所有支付记录列表。
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments() {
        return paymentTransactionJpaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(PaymentTransactionEntity transaction) {
        OrderMainEntity order = orderMainRepository.findById(transaction.getOrderId())
                .orElse(null);
        return new PaymentResponse(
                transaction.getPaymentNo(),
                order != null ? order.getOrderNo() : null,
                transaction.getStatus(),
                transaction.getPaidAt()
        );
    }

    private String json(Map<String, ?> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Serialize payment payload failed", ex);
        }
    }

    private String nextPaymentNo() {
        return "PAY" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
