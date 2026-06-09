package com.zhangyuan.modules.payment.application.service;

import com.zhangyuan.common.exception.NotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public PaymentResponse getPayment(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentNo));
        return toResponse(payment);
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
            throw new IllegalArgumentException("Only mock payment channel is supported");
        }
        Order order = orderRepository.findByOrderNo(new OrderNumber(request.orderNo().trim()))
                .orElseThrow(() -> new NotFoundException("Order not found: " + request.orderNo()));
        if (!order.isPending()) {
            throw new IllegalArgumentException("Order is not payable");
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
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentNo));
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found for payment: " + paymentNo));
        if (!payment.isSuccess()) {
            order.markPaid();
            payment.markSuccess();
            orderRepository.save(order);
            paymentRepository.save(payment);
            log.info("Order {} and payment {} persisted as paid", order.getOrderNo(), paymentNo);
        }
        return new PaymentResponse(payment.getPaymentNo(), order.getOrderNo().value(), payment.getStatus().name().toLowerCase(), payment.getPaidAt(),
                payment.getChannel(), payment.getAmount(), payment.getCurrency(), payment.getCreatedAt());
    }

    /**
     * 获取支付记录列表（分页）。
     */
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listPayments(int page, int pageSize) {
        var pageResult = paymentRepository.findPageByCreatedAtDesc(page, pageSize);
        List<Payment> payments = pageResult.items();
        Map<Long, Order> orderMap = batchFetchOrders(payments);
        List<PaymentResponse> items = payments.stream()
                .map(p -> toResponse(p, orderMap.get(p.getOrderId())))
                .toList();
        return PageResponse.of(items, pageResult.page(), pageResult.pageSize(), pageResult.total());
    }

    private Map<Long, Order> batchFetchOrders(List<Payment> payments) {
        List<Long> orderIds = payments.stream()
                .map(Payment::getOrderId)
                .distinct()
                .toList();
        return orderRepository.findAllById(orderIds);
    }

    private PaymentResponse toResponse(Payment payment) {
        Order order = orderRepository.findById(payment.getOrderId())
                .orElse(null);
        return toResponse(payment, order);
    }

    private PaymentResponse toResponse(Payment payment, Order order) {
        return new PaymentResponse(
                payment.getPaymentNo(),
                order != null ? order.getOrderNo().value() : null,
                payment.getStatus().name().toLowerCase(),
                payment.getPaidAt(),
                payment.getChannel(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getCreatedAt()
        );
    }

    private String nextPaymentNo() {
        return "PAY" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
