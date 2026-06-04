package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.client.FulfillmentClient;
import com.zhangyuan.payment.client.OrderServiceClient;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.repository.PaymentRepository;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import com.zhangyuan.payment.dto.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentApplicationService {
    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);
    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;
    private final FulfillmentClient fulfillmentClient;

    public PaymentApplicationService(PaymentRepository paymentRepository,
                                     OrderServiceClient orderServiceClient,
                                     FulfillmentClient fulfillmentClient) {
        this.paymentRepository = paymentRepository;
        this.orderServiceClient = orderServiceClient;
        this.fulfillmentClient = fulfillmentClient;
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        // Verify order exists via Feign
        var orderResp = orderServiceClient.getOrder(request.orderNo());
        if (orderResp == null || orderResp.code() != 0) {
            throw new IllegalArgumentException("Order not found: " + request.orderNo());
        }

        // Only support mock channel for now
        if (!"mock".equals(request.channel())) {
            throw new IllegalArgumentException("Unsupported channel: " + request.channel());
        }

        String paymentNo = "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
        Payment payment = new Payment(paymentNo, 0L, request.orderNo(), "mock", BigDecimal.ZERO, "CNY");
        payment = paymentRepository.save(payment);

        String mockPayUrl = "/api/payments/mock/" + paymentNo + "/success";
        return new CheckoutResponse(paymentNo, payment.getStatus(), mockPayUrl, null);
    }

    @Transactional
    public CheckoutResponse mockSuccess(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));
        payment.markSuccess();
        payment = paymentRepository.save(payment);

        // Trigger order fulfillment after successful payment
        try {
            fulfillmentClient.fulfillOrder(payment.getOrderNo());
            log.info("Fulfillment triggered for order: {}", payment.getOrderNo());
        } catch (Exception e) {
            log.error("Fulfillment failed for order: {}", payment.getOrderNo(), e);
        }

        return new CheckoutResponse(paymentNo, payment.getStatus(), null, null);
    }

    public PaymentResponse getPayment(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo)
                .map(p -> new PaymentResponse(p.getPaymentNo(), p.getOrderNo(), p.getStatus(), p.getPaidAt()))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));
    }
}
