package com.zhangyuan.modules.order.adapter.out.persistence;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import com.zhangyuan.modules.order.domain.model.OrderStatus;
import com.zhangyuan.modules.order.domain.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaOrderRepository implements OrderRepository {

    private final OrderMainEntityRepository jpaRepository;

    public JpaOrderRepository(OrderMainEntityRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Map<Long, Order> findAllById(Collection<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(this::toDomain)
                .collect(Collectors.toMap(Order::getId, o -> o));
    }

    @Override
    public Optional<Order> findByOrderNo(OrderNumber orderNo) {
        return jpaRepository.findByOrderNo(orderNo.value()).map(this::toDomain);
    }

    @Override
    public List<Order> findAllOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResponse<Order> findPageByCreatedAtDesc(int page, int pageSize) {
        Page<OrderMainEntity> pageResult = jpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, pageSize));
        List<Order> items = pageResult.getContent().stream()
                .map(this::toDomain)
                .toList();
        return PageResponse.of(items, page, pageSize, pageResult.getTotalElements());
    }

    @Override
    public Order save(Order order) {
        OrderMainEntity entity;
        if (order.getId() != null) {
            entity = jpaRepository.findById(order.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + order.getId()));
            entity.setStatus(order.getStatus().name().toLowerCase());
            entity.setPaidAt(order.getPaidAt());
            entity.setCancelledAt(order.getCancelledAt());
            entity.setSnapshotJson(order.getSnapshotJson());
        } else {
            entity = new OrderMainEntity(
                    order.getOrderNo().value(),
                    order.getPlanId(),
                    order.getPriceId(),
                    order.getAmount(),
                    order.getCurrency(),
                    order.getSnapshotJson()
            );
            entity.setUserId(order.getUserId());
        }
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private Order toDomain(OrderMainEntity entity) {
        String statusStr = entity.getStatus();
        OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
        Order order = Order.reconstitute(
                new OrderNumber(entity.getOrderNo()),
                entity.getUserId(),
                entity.getPlanId(),
                entity.getPriceId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getSnapshotJson(),
                status,
                entity.getCreatedAt(),
                entity.getPaidAt(),
                entity.getCancelledAt()
        );
        order.setId(entity.getId());
        return order;
    }
}
