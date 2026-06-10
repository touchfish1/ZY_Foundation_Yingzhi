package com.zhangyuan.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.common.exception.NotFoundException;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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
    public Order createOrder(Long userId, Long planId, Long priceId, java.math.BigDecimal amount, String currency, String snapshotJson) {
        log.info("Creating order: userId={}, planId={}, priceId={}, amount={}, currency={}", userId, planId, priceId, amount, currency);
        Order order = orderDomainService.createOrder(userId, planId, priceId, amount, currency, snapshotJson);
        Order saved = orderRepository.save(order);
        log.info("Order created: orderNo={}", saved.getOrderNo());
        return saved;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        return createOrder(request, null);
    }

    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        log.info("Creating order: planCode={}, billingCycle={}, currency={}, userId={}", request.planCode(), request.billingCycle(), request.currency(), userId);
        String currency = request.currency().trim().toUpperCase();
        String billingCycle = request.billingCycle().trim();
        String planCode = request.planCode().trim();

        // Feign call outside transaction to avoid holding DB connection pool
        var resp = productClient.getPlanGroup(planCode);
        if (resp == null || resp.code() != 0) {
            throw new NotFoundException("Product plan not found: " + planCode);
        }

        SnapShotResult snapshotResult = buildSnapshotFromFeignResponse(resp.data(), planCode, billingCycle, currency);

        if (snapshotResult.planId() == null || snapshotResult.priceId() == null) {
            log.error("Plan/price not found for planCode={}, billingCycle={}, currency={}", planCode, billingCycle, currency);
            throw new IllegalArgumentException(
                    "No matching plan/price found for planCode=" + planCode + ", billingCycle=" + billingCycle + ", currency=" + currency);
        }

        return doCreateOrder(userId, snapshotResult.planId(), snapshotResult.priceId(), snapshotResult.amount(), currency, snapshotResult.snapshotJson());
    }

    @Transactional
    public OrderResponse doCreateOrder(Long userId, Long planId, Long priceId, java.math.BigDecimal amount, String currency, String snapshotJson) {
        Order order = orderDomainService.createOrder(userId, planId, priceId, amount, currency, snapshotJson);
        Order saved = orderRepository.save(order);
        log.info("Order created: orderNo={}, amount={}, userId={}", saved.getOrderNo(), saved.getAmount(), userId);
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
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNo));
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

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
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
                order.getId(),
                order.getOrderNo().value(),
                order.getUserId(),
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

    /**
     * Extract pricing info from the Feign response data and build an enriched snapshot JSON.
     * The response data from getPlanGroup is a Map (due to Feign generic erasure) structured as:
     *  PlanGroupResponse { code, name, plans: [{ code, name, prices: [{ billingCycle, currency, amount }] }] }
     */
    @SuppressWarnings("unchecked")
    private SnapShotResult buildSnapshotFromFeignResponse(Object data, String planCode, String billingCycle, String currency) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("planCode", planCode);
            snapshot.put("billingCycle", billingCycle);
            snapshot.put("currency", currency);
            snapshot.put("createdAt", Instant.now().toString());

            // Default values
            BigDecimal amount = BigDecimal.ZERO;
            String planName = planCode;
            int validityDays = getValidityDays(billingCycle);
            Long planId = null;
            Long priceId = null;

            if (data instanceof Map) {
                Map<String, Object> groupMap = (Map<String, Object>) data;
                Object plansObj = groupMap.get("plans");
                if (plansObj instanceof List) {
                    for (Object planObj : (List<Object>) plansObj) {
                        if (planObj instanceof Map) {
                            Map<String, Object> planMap = (Map<String, Object>) planObj;
                            String pCode = (String) planMap.get("code");
                            if (planCode.equals(pCode)) {
                                planName = (String) planMap.getOrDefault("name", planCode);
                                Object pid = planMap.get("id");
                                if (pid instanceof Number) {
                                    planId = ((Number) pid).longValue();
                                }
                                // Find matching price for the given billing cycle and currency
                                Object pricesObj = planMap.get("prices");
                                if (pricesObj instanceof List) {
                                    for (Object priceObj : (List<Object>) pricesObj) {
                                        if (priceObj instanceof Map) {
                                            Map<String, Object> priceMap = (Map<String, Object>) priceObj;
                                            if (billingCycle.equals(priceMap.get("billingCycle"))
                                                    && currency.equals(priceMap.get("currency"))) {
                                                Object amt = priceMap.get("amount");
                                                if (amt instanceof Number) {
                                                    amount = new BigDecimal(((Number) amt).toString());
                                                }
                                                Object prid = priceMap.get("id");
                                                if (prid instanceof Number) {
                                                    priceId = ((Number) prid).longValue();
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }

            snapshot.put("planName", planName);
            snapshot.put("validityDays", validityDays);
            snapshot.put("amount", amount);

            log.debug("Built snapshot for planCode={}: planName={}, validityDays={}, amount={}",
                    planCode, planName, validityDays, amount);
            return new SnapShotResult(planId, priceId, objectMapper.writeValueAsString(snapshot), amount);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Create order snapshot failed", ex);
        }
    }

    private int getValidityDays(String billingCycle) {
        return switch (billingCycle.toLowerCase()) {
            case "monthly", "month" -> 30;
            case "yearly", "year", "annual" -> 365;
            case "quarterly", "quarter" -> 90;
            case "weekly", "week" -> 7;
            case "daily", "day" -> 1;
            default -> 30;
        };
    }

    /**
     * Internal record holding the snapshot JSON string and the extracted amount.
     */
    private record SnapShotResult(Long planId, Long priceId, String snapshotJson, BigDecimal amount) {}

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void autoCancelExpiredOrders() {
        Instant expireThreshold = Instant.now().minus(30, java.time.temporal.ChronoUnit.MINUTES);
        var expiredOrders = orderRepository.findPendingOrdersOlderThan(expireThreshold, 100);
        for (var order : expiredOrders) {
            try {
                order.cancel();
                orderRepository.save(order);
                log.info("Auto-cancelled expired order: {}", order.getOrderNo());
            } catch (Exception e) {
                log.error("Failed to auto-cancel order: {}", order.getOrderNo(), e);
            }
        }
    }
}
