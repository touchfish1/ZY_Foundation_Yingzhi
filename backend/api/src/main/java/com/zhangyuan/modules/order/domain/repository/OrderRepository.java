package com.zhangyuan.modules.order.domain.repository;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNo(OrderNumber orderNo);

    List<Order> findAllOrderByCreatedAtDesc();

    Map<Long, Order> findAllById(Collection<Long> ids);

    PageResponse<Order> findPageByCreatedAtDesc(int page, int pageSize);

    Order save(Order order);
}
