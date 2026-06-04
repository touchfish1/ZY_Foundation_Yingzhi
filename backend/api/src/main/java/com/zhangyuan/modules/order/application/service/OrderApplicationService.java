package com.zhangyuan.modules.order.application.service;

import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import com.zhangyuan.modules.order.domain.repository.OrderRepository;
import com.zhangyuan.modules.order.domain.service.OrderDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 订单应用服务，负责订单创建和支付标记的编排。
 */
@Service
public class OrderApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;

    public OrderApplicationService(OrderRepository orderRepository, OrderDomainService orderDomainService) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
    }

    /**
     * 创建订单。
     *
     * @param planId        产品方案 ID
     * @param priceId       价格 ID
     * @param amount        订单金额
     * @param currency      货币类型
     * @param snapshotJson  快照 JSON
     * @return 创建的订单
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
     * 根据订单号查询订单。
     *
     * @param orderNo 订单号
     * @return 订单 Optional
     */
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(new OrderNumber(orderNo));
    }

    /**
     * 获取所有订单列表。
     *
     * @return 订单列表
     */
    @Transactional(readOnly = true)
    public List<Order> listAll() {
        return orderRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 将指定订单标记为已支付。
     *
     * @param orderNo 订单号
     * @return 更新后的订单
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
}
