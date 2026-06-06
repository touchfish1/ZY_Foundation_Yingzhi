package com.zhangyuan.modules.payment.application.service;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import com.zhangyuan.modules.order.domain.repository.OrderRepository;
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
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentDomainService paymentDomainService;
    private final OrderRepository orderRepository;

    public PaymentApplicationService(PaymentRepository paymentRepository,
                                     PaymentDomainService paymentDomainService,
                                     OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentDomainService = paymentDomainService;
        this.orderRepository = orderRepository;
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
        Order order = orderRepository.findByOrderNo(new OrderNumber(request.orderNo().trim()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.isPending()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not payable");
        }

        Payment payment = new Payment(
                nextPaymentNo(),
                order.getId(),
                "mock",
                order.getAmount(),
                order.getCurrency()
        );
        payment = paymentRepository.save(payment);
        String mockPayUrl = "/api/payments/mock/" + payment.getPaymentNo() + "/success";
        return new CheckoutResponse(payment.getPaymentNo(), payment.getStatus().name().toLowerCase(), mockPayUrl, mockPayUrl);
    }

    /**
     * 模拟支付成功，更新支付和订单状态。
     */
    @Transactional
    public PaymentResponse mockSuccess(String paymentNo) {
        log.info("Mock payment success: {}", paymentNo);
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!payment.isSuccess()) {
            order.markPaid();
            payment.markSuccess();
        }
        return new PaymentResponse(payment.getPaymentNo(), order.getOrderNo().value(), payment.getStatus().name().toLowerCase(), payment.getPaidAt());
    }

    /**
     * 获取支付记录列表（分页）。
     */
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listPayments(int page, int pageSize) {
        var pageResult = paymentRepository.findPageByCreatedAtDesc(page, pageSize);
        List<PaymentResponse> items = pageResult.items().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(items, pageResult.page(), pageResult.pageSize(), pageResult.total());
    }

    private PaymentResponse toResponse(Payment payment) {
        Order order = orderRepository.findById(payment.getOrderId())
                .orElse(null);
        return new PaymentResponse(
                payment.getPaymentNo(),
                order != null ? order.getOrderNo().value() : null,
                payment.getStatus().name().toLowerCase(),
                payment.getPaidAt()
        );
    }

    private String nextPaymentNo() {
        return "PAY" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
