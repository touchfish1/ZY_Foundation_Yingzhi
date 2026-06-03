package com.zhangyuan.modules.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.modules.order.domain.OrderMain;
import com.zhangyuan.modules.order.dto.CreateOrderRequest;
import com.zhangyuan.modules.order.dto.OrderResponse;
import com.zhangyuan.modules.order.repository.OrderMainRepository;
import com.zhangyuan.modules.product.domain.ProductPlan;
import com.zhangyuan.modules.product.domain.ProductPrice;
import com.zhangyuan.modules.product.repository.ProductPlanRepository;
import com.zhangyuan.modules.product.repository.ProductPriceRepository;
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
import java.util.UUID;

/**
 * 订单服务，提供订单的创建、查询和快照生成功能。
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderMainRepository orderMainRepository;
    private final ProductPlanRepository productPlanRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderMainRepository orderMainRepository,
                        ProductPlanRepository productPlanRepository,
                        ProductPriceRepository productPriceRepository,
                        ObjectMapper objectMapper) {
        this.orderMainRepository = orderMainRepository;
        this.productPlanRepository = productPlanRepository;
        this.productPriceRepository = productPriceRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建订单，查找产品方案和价格后生成订单快照。
     *
     * @param request 创建订单请求
     * @return 订单响应
     */
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        log.info("Creating order: planCode={}, billingCycle={}, currency={}", request.planCode(), request.billingCycle(), request.currency());
        String currency = request.currency().trim().toUpperCase();
        String billingCycle = request.billingCycle().trim();
        // 查找启用的产品方案
        ProductPlan plan = productPlanRepository.findByCode(request.planCode().trim())
                .filter(item -> "enabled".equals(item.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product plan not found"));
        // 查找匹配的价格配置
        ProductPrice price = productPriceRepository
                .findFirstByPlanIdAndBillingCycleAndCurrencyAndStatus(plan.getId(), billingCycle, currency, "enabled")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product price not found"));

        // 创建订单并生成快照
        OrderMain order = new OrderMain(
                nextOrderNo(),
                plan.getId(),
                price.getId(),
                price.getAmount(),
                price.getCurrency(),
                snapshot(plan, price)
        );
        return toResponse(orderMainRepository.save(order));
    }

    /**
     * 根据订单号查询订单实体。
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    @Transactional(readOnly = true)
    public OrderMain getByOrderNo(String orderNo) {
        return orderMainRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    /**
     * 根据订单号查询订单响应。
     *
     * @param orderNo 订单号
     * @return 订单响应
     */
    @Transactional(readOnly = true)
    public OrderResponse get(String orderNo) {
        return toResponse(getByOrderNo(orderNo));
    }

    /**
     * 获取所有订单列表。
     *
     * @return 订单响应列表
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders() {
        return orderMainRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 将订单实体转换为响应对象。
     */
    public OrderResponse toResponse(OrderMain order) {
        return new OrderResponse(
                order.getOrderNo(),
                order.getPlanId(),
                order.getPriceId(),
                order.getAmount(),
                order.getCurrency(),
                order.getStatus(),
                order.getSnapshotJson(),
                order.getCreatedAt(),
                order.getPaidAt()
        );
    }

    /**
     * 生成产品和价格的快照 JSON，用于订单历史追溯。
     */
    private String snapshot(ProductPlan plan, ProductPrice price) {
        try {
            // 构建产品方案快照
            Map<String, Object> planSnapshot = new LinkedHashMap<>();
            planSnapshot.put("id", plan.getId());
            planSnapshot.put("code", plan.getCode());
            planSnapshot.put("name", plan.getName());
            planSnapshot.put("description", plan.getDescription());
            planSnapshot.put("badge", plan.getBadge());
            // 构建价格快照
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

    /**
     * 生成下一个订单号（ORD + 时间戳 + 随机字符）。
     */
    private String nextOrderNo() {
        return "ORD" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
