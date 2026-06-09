package com.zhangyuan.modules.order.domain.service;

import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 订单领域服务，包含订单创建和支付验证的核心业务规则。
 */
@Service
public class OrderDomainService {

    private static final Logger log = LoggerFactory.getLogger(OrderDomainService.class);

    /**
     * 创建订单领域对象并生成订单号。
     *
     * @param planId        产品方案 ID
     * @param priceId       价格 ID
     * @param amount        订单金额
     * @param currency      货币类型
     * @param snapshotJson  快照 JSON
     * @return 订单领域对象
     */
    public Order createOrder(Long planId, Long priceId, BigDecimal amount, String currency, String snapshotJson) {
        return createOrder(null, planId, priceId, amount, currency, snapshotJson);
    }

    public Order createOrder(Long userId, Long planId, Long priceId, BigDecimal amount, String currency, String snapshotJson) {
        OrderNumber orderNo = OrderNumber.generate();
        log.info("Domain order created: orderNo={}, userId={}", orderNo.value(), userId);
        return new Order(orderNo, userId, planId, priceId, amount, currency, snapshotJson);
    }

    /**
     * 验证订单是否处于待支付状态。
     *
     * @param order 订单领域对象
     * @throws IllegalStateException 订单状态不正确时抛出
     */
    public void validatePayment(Order order) {
        if (!order.isPending()) {
            log.warn("Order {} is not pending, cannot pay", order.getOrderNo());
            throw new IllegalStateException("Order " + order.getOrderNo() + " is not pending");
        }
    }
}
