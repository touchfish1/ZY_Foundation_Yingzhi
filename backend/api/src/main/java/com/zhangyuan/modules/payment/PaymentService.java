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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务，提供支付下单、模拟支付成功等功能。
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

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

    /**
     * 发起支付下单（仅支持 Mock 模拟支付）。
     *
     * @param request 下单请求
     * @return 支付下单响应（含模拟支付 URL）
     */
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        log.info("Checkout: orderNo={}, channel={}", request.orderNo(), request.channel());
        // 仅支持 Mock 通道
        if (!"mock".equalsIgnoreCase(request.channel().trim())) {
            log.warn("Only mock payment channel is supported, got: {}", request.channel());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only mock payment channel is supported");
        }
        // 查找订单并验证可支付状态
        OrderMain order = orderMainRepository.findByOrderNo(request.orderNo().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!"pending".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not payable");
        }

        // 创建支付交易记录
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

    /**
     * 获取所有支付记录列表。
     *
     * @return 支付记录响应列表
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments() {
        return paymentTransactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(PaymentTransaction transaction) {
        OrderMain order = orderMainRepository.findById(transaction.getOrderId())
                .orElse(null);
        return new PaymentResponse(
                transaction.getPaymentNo(),
                order != null ? order.getOrderNo() : null,
                transaction.getStatus(),
                transaction.getPaidAt()
        );
    }

    /**
     * 模拟支付成功，更新支付和订单状态。
     *
     * @param paymentNo 支付单号
     * @return 支付结果
     */
    @Transactional
    public PaymentResponse mockSuccess(String paymentNo) {
        log.info("Mock payment success: {}", paymentNo);
        PaymentTransaction payment = paymentTransactionRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        OrderMain order = orderMainRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        // 仅当支付状态不是已支付时才标记
        if (!"paid".equals(payment.getStatus())) {
            Instant now = Instant.now();
            payment.markPaid(json(Map.of("mockResult", "success", "paidAt", now.toString())), now);
            order.markPaid(now);
        }
        return new PaymentResponse(payment.getPaymentNo(), order.getOrderNo(), payment.getStatus(), payment.getPaidAt());
    }

    /**
     * 将 Map 序列化为 JSON 字符串。
     */
    private String json(Map<String, ?> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Serialize payment payload failed", ex);
        }
    }

    /**
     * 生成下一个支付单号（PAY + 时间戳 + 随机字符）。
     */
    private String nextPaymentNo() {
        return "PAY" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
