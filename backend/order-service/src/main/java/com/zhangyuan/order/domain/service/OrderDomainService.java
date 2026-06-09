package com.zhangyuan.order.domain.service;

import com.zhangyuan.order.domain.model.Order;
import com.zhangyuan.order.domain.model.OrderNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderDomainService {

    private static final Logger log = LoggerFactory.getLogger(OrderDomainService.class);

    public Order createOrder(Long userId, Long planId, Long priceId, BigDecimal amount, String currency, String snapshotJson) {
        OrderNumber orderNo = OrderNumber.generate();
        log.info("Domain order created: orderNo={}, userId={}", orderNo.value(), userId);
        return new Order(orderNo, userId, planId, priceId, amount, currency, snapshotJson);
    }

    public void validatePayment(Order order) {
        if (!order.isPending()) {
            log.warn("Order {} is not pending, cannot pay", order.getOrderNo());
            throw new IllegalStateException("Order " + order.getOrderNo() + " is not pending");
        }
    }
}
