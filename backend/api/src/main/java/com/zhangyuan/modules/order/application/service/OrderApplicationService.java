package com.zhangyuan.modules.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import com.zhangyuan.modules.order.domain.repository.OrderRepository;
import com.zhangyuan.modules.order.domain.service.OrderDomainService;
import com.zhangyuan.modules.order.dto.CreateOrderRequest;
import com.zhangyuan.modules.order.dto.OrderResponse;
import com.zhangyuan.modules.product.adapter.out.persistence.ProductPlan;
import com.zhangyuan.modules.product.adapter.out.persistence.ProductPrice;
import com.zhangyuan.modules.product.repository.ProductPlanRepository;
import com.zhangyuan.modules.product.repository.ProductPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 订单应用服务，负责订单创建和支付的编排。
 */
@Service
public class OrderApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final ProductPlanRepository productPlanRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ObjectMapper objectMapper;

    public OrderApplicationService(OrderRepository orderRepository,
                                   OrderDomainService orderDomainService,
                                   ProductPlanRepository productPlanRepository,
                                   ProductPriceRepository productPriceRepository,
                                   ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
        this.productPlanRepository = productPlanRepository;
        this.productPriceRepository = productPriceRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 使用原始参数创建订单（DDD 内部使用）。
     */
    @Transactional
    public Order createOrder(Long planId, Long priceId, BigDecimal amount, String currency, String snapshotJson) {
        log.info("Creating order: planId={}, priceId={}, amount={}, currency={}", planId, priceId, amount, currency);
        Order order = orderDomainService.createOrder(planId, priceId, amount, currency, snapshotJson);
        Order saved = orderRepository.save(order);
        log.info("Order created: orderNo={}", saved.getOrderNo());
        return saved;
    }

    /**
     * 根据 CreateOrderRequest 创建订单，执行产品方案和价格查询及快照生成。
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order: planCode={}, billingCycle={}, currency={}", request.planCode(), request.billingCycle(), request.currency());
        String currency = request.currency().trim().toUpperCase();
        String billingCycle = request.billingCycle().trim();

        ProductPlan plan = productPlanRepository.findByCode(request.planCode().trim())
                .filter(item -> "enabled".equals(item.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product plan not found"));

        ProductPrice price = productPriceRepository
                .findFirstByPlanIdAndBillingCycleAndCurrencyAndStatus(plan.getId(), billingCycle, currency, "enabled")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product price not found"));

        String snapshotJson = snapshot(plan, price);
        Order order = orderDomainService.createOrder(plan.getId(), price.getId(), price.getAmount(), price.getCurrency(), snapshotJson);
        Order saved = orderRepository.save(order);
        log.info("Order created: orderNo={}", saved.getOrderNo());
        return toResponse(saved);
    }

    /**
     * 根据订单号查询订单。
     */
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(new OrderNumber(orderNo));
    }

    /**
     * 根据订单号查询订单响应。
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNo) {
        return orderRepository.findByOrderNo(new OrderNumber(orderNo))
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    /**
     * 获取所有订单列表（领域对象）。
     */
    @Transactional(readOnly = true)
    public List<Order> listAll() {
        return orderRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 获取所有订单列表（响应对象）。
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders() {
        return orderRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 将指定订单标记为已支付。
     */
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

    /**
     * 将订单领域对象转换为响应对象。
     */
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

    /**
     * 生成产品和价格的快照 JSON。
     */
    private String snapshot(ProductPlan plan, ProductPrice price) {
        try {
            Map<String, Object> planSnapshot = new LinkedHashMap<>();
            planSnapshot.put("id", plan.getId());
            planSnapshot.put("code", plan.getCode());
            planSnapshot.put("name", plan.getName());
            planSnapshot.put("description", plan.getDescription());
            planSnapshot.put("badge", plan.getBadge());

            Map<String, Object> priceSnapshot = new LinkedHashMap<>();
            priceSnapshot.put("id", price.getId());
            priceSnapshot.put("billingCycle", price.getBillingCycle());
            priceSnapshot.put("currency", price.getCurrency());
            priceSnapshot.put("amount", price.getAmount());
            priceSnapshot.put("originalAmount", price.getOriginalAmount());

            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("plan", planSnapshot);
            snapshot.put("price", priceSnapshot);
            snapshot.put("createdAt", Instant.now().toString());
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Create order snapshot failed", ex);
        }
    }
}
