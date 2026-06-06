package com.zhangyuan.modules.order.domain.repository;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNo(OrderNumber orderNo);

    List<Order> findAllOrderByCreatedAtDesc();

    PageResponse<Order> findPageByCreatedAtDesc(int page, int pageSize);

    Order save(Order order);
}
