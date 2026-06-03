package com.zhangyuan.modules.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.modules.order.domain.OrderMain;
import com.zhangyuan.modules.order.repository.OrderMainRepository;
import com.zhangyuan.modules.payment.domain.PaymentTransaction;
import com.zhangyuan.modules.payment.dto.CheckoutRequest;
import com.zhangyuan.modules.payment.dto.CheckoutResponse;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import com.zhangyuan.modules.payment.repository.PaymentTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderMainRepository orderMainRepository;
    private final ObjectMapper objectMapper;

    public PaymentService(PaymentTransactionRepository paymentTransactionRepository,
                          OrderMainRepository orderMainRepository,
                          ObjectMapper objectMapper) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderMainRepository = orderMainRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        if (!"mock".equalsIgnoreCase(request.channel().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only mock payment channel is supported");
        }
        OrderMain order = orderMainRepository.findByOrderNo(request.orderNo().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!"pending".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not payable");
        }

        PaymentTransaction payment = new PaymentTransaction(
                nextPaymentNo(),
                order.getId(),
                "mock",
                order.getAmount(),
                order.getCurrency(),
                json(Map.of("orderNo", order.getOrderNo(), "channel", "mock", "createdAt", Instant.now().toString()))
        );
        payment = paymentTransactionRepository.save(payment);
        String mockPayUrl = "/api/payments/mock/" + payment.getPaymentNo() + "/success";
        return new CheckoutResponse(payment.getPaymentNo(), payment.getStatus(), mockPayUrl, mockPayUrl);
    }

    @Transactional
    public PaymentResponse mockSuccess(String paymentNo) {
        PaymentTransaction payment = paymentTransactionRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        OrderMain order = orderMainRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!"paid".equals(payment.getStatus())) {
            Instant now = Instant.now();
            payment.markPaid(json(Map.of("mockResult", "success", "paidAt", now.toString())), now);
            order.markPaid(now);
        }
        return new PaymentResponse(payment.getPaymentNo(), order.getOrderNo(), payment.getStatus(), payment.getPaidAt());
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
