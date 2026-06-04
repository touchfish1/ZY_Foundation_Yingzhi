package com.zhangyuan.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.order.client.ProductServiceClient;
import com.zhangyuan.order.domain.model.Order;
import com.zhangyuan.order.domain.model.OrderNumber;
import com.zhangyuan.order.domain.repository.OrderRepository;
import com.zhangyuan.order.domain.service.OrderDomainService;
import com.zhangyuan.order.dto.CreateOrderRequest;
import com.zhangyuan.order.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final ProductServiceClient productClient;
    private final ObjectMapper objectMapper;

    public OrderApplicationService(OrderRepository orderRepository,
                                   OrderDomainService orderDomainService,
                                   ProductServiceClient productClient,
                                   ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
        this.productClient = productClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Order createOrder(Long planId, Long priceId, java.math.BigDecimal amount, String currency, String snapshotJson) {
        log.info("Creating order: planId={}, priceId={}, amount={}, currency={}", planId, priceId, amount, currency);
        Order order = orderDomainService.createOrder(planId, priceId, amount, currency, snapshotJson);
        Order saved = orderRepository.save(order);
        log.info("Order created: orderNo={}", saved.getOrderNo());
        return saved;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order: planCode={}, billingCycle={}, currency={}", request.planCode(), request.billingCycle(), request.currency());
        String currency = request.currency().trim().toUpperCase();
        String billingCycle = request.billingCycle().trim();

        // Verify product exists via Feign client
        var resp = productClient.getPlanGroup(request.planCode().trim());
        if (resp == null || resp.code() != 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product plan not found: " + request.planCode());
        }

        String snapshotJson = snapshotFromFeignResponse(resp.data(), request.planCode().trim(), billingCycle, currency);
        Order order = orderDomainService.createOrder(null, null, java.math.BigDecimal.ZERO, currency, snapshotJson);
        Order saved = orderRepository.save(order);
        log.info("Order created: orderNo={}", saved.getOrderNo());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(new OrderNumber(orderNo));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNo) {
        return orderRepository.findByOrderNo(new OrderNumber(orderNo))
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> listAll() {
        return orderRepository.findAllOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders() {
        return orderRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Order markPaid(String orderNo) {
        log.info("Marking order as paid: {}", orderNo);
        Order order = orderRepository.findByOrderNo(new OrderNumber(orderNo))
                .orElseThrow(() -> {
                    log.error("Order not found: {}", orderNo);
                    return new IllegalArgumentException("Order not found: " + orderNo);
                });
        orderDomainService.validatePayment(order);
        order.markPaid();
        Order saved = orderRepository.save(order);
        log.info("Order marked as paid: {}", orderNo);
        return saved;
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getOrderNo().value(),
                order.getPlanId(),
                order.getPriceId(),
                order.getAmount(),
                order.getCurrency(),
                order.getStatus().name().toLowerCase(),
                order.getSnapshotJson(),
                order.getCreatedAt(),
                order.getPaidAt()
        );
    }

    @SuppressWarnings("unchecked")
    private String snapshotFromFeignResponse(Object data, String planCode, String billingCycle, String currency) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("planCode", planCode);
            snapshot.put("billingCycle", billingCycle);
            snapshot.put("currency", currency);
            snapshot.put("createdAt", Instant.now().toString());

            if (data instanceof Map) {
                snapshot.put("planGroup", (Map<String, Object>) data);
            }

            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Create order snapshot failed", ex);
        }
    }
}
