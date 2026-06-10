package com.zhangyuan.order.domain.repository;

import com.zhangyuan.order.domain.model.Order;
import com.zhangyuan.order.domain.model.OrderNumber;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNo(OrderNumber orderNo);

    List<Order> findAllOrderByCreatedAtDesc();

    List<Order> findPendingOrdersOlderThan(Instant threshold, int limit);

    List<Order> findByUserId(Long userId);

    Order save(Order order);
}
