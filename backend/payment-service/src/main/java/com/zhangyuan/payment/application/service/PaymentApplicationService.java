package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.client.FulfillmentClient;
import com.zhangyuan.payment.client.OrderServiceClient;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.repository.PaymentRepository;
import com.zhangyuan.payment.domain.service.PaymentChannelStrategy;
import com.zhangyuan.payment.domain.service.PaymentDomainService;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import com.zhangyuan.payment.dto.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentApplicationService {
    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);
    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;
    private final FulfillmentClient fulfillmentClient;
    private final PaymentDomainService paymentDomainService;
    private final ChannelStrategyRegistry strategyRegistry;
    private final CompensationService compensationService;

    public PaymentApplicationService(PaymentRepository paymentRepository,
                                     OrderServiceClient orderServiceClient,
                                     FulfillmentClient fulfillmentClient,
                                     PaymentDomainService paymentDomainService,
                                     ChannelStrategyRegistry strategyRegistry,
                                     CompensationService compensationService) {
        this.paymentRepository = paymentRepository;
        this.orderServiceClient = orderServiceClient;
        this.fulfillmentClient = fulfillmentClient;
        this.paymentDomainService = paymentDomainService;
        this.strategyRegistry = strategyRegistry;
        this.compensationService = compensationService;
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        var orderResp = orderServiceClient.getOrder(request.orderNo());
        if (orderResp == null || orderResp.code() != 0) {
            throw new IllegalArgumentException("Order not found: " + request.orderNo());
        }

        PaymentChannelStrategy strategy = strategyRegistry.getStrategy(request.channel());
        BigDecimal amount = BigDecimal.ZERO;
        Long orderId = 0L;
        try {
            var orderData = (java.util.Map<String, Object>) orderResp.data();
            if (orderData != null) {
                if (orderData.get("amount") != null) {
                    amount = new BigDecimal(orderData.get("amount").toString());
                }
                if (orderData.get("id") != null) {
                    orderId = ((Number) orderData.get("id")).longValue();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract order data, using defaults: {}", e.getMessage());
        }

        Payment payment = paymentDomainService.createPayment(orderId, request.orderNo(), request.channel(), amount, "CNY");
        payment = paymentRepository.save(payment);

        return strategy.createPayment(payment, request);
    }

    @Transactional
    public CheckoutResponse mockSuccess(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));

        if ("SUCCESS".equals(payment.getStatus())) {
            log.warn("Idempotent call: payment {} already SUCCESS, skipped", paymentNo);
            return new CheckoutResponse(paymentNo, payment.getStatus(), null, null);
        }

        PaymentChannelStrategy strategy = strategyRegistry.getStrategy(payment.getChannel());
        strategy.processCallback(payment, java.util.Map.of());
        payment = paymentRepository.save(payment);

        // Notify order-service to mark order as paid
        try {
            fulfillmentClient.markPaid(payment.getOrderNo());
            log.info("Order marked as paid: {}", payment.getOrderNo());
        } catch (Exception e) {
            log.error("Failed to mark order as paid: {}", payment.getOrderNo(), e);
        }

        compensationService.createFulfillEvent(paymentNo, payment.getOrderNo());

        try {
            fulfillmentClient.fulfillOrder(payment.getOrderNo());
            log.info("Fulfillment triggered for order: {}", payment.getOrderNo());
        } catch (Exception e) {
            log.error("Fulfillment failed for order: {}, compensation event created", payment.getOrderNo(), e);
        }

        return new CheckoutResponse(paymentNo, payment.getStatus(), null, null);
    }

    public PaymentResponse getPayment(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo)
                .map(p -> new PaymentResponse(p.getPaymentNo(), p.getOrderNo(), p.getChannel(), p.getAmount(), p.getCurrency(), p.getStatus(), p.getCreatedAt(), p.getPaidAt()))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));
    }
}
