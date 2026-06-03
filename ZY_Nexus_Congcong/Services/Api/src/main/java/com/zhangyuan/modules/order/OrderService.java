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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

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

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        String currency = request.currency().trim().toUpperCase();
        String billingCycle = request.billingCycle().trim();
        ProductPlan plan = productPlanRepository.findByCode(request.planCode().trim())
                .filter(item -> "enabled".equals(item.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product plan not found"));
        ProductPrice price = productPriceRepository
                .findFirstByPlanIdAndBillingCycleAndCurrencyAndStatus(plan.getId(), billingCycle, currency, "enabled")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product price not found"));

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

    @Transactional(readOnly = true)
    public OrderMain getByOrderNo(String orderNo) {
        return orderMainRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(String orderNo) {
        return toResponse(getByOrderNo(orderNo));
    }

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

    private String nextOrderNo() {
        return "ORD" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
