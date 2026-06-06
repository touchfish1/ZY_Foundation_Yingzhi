package com.zhangyuan.order.adapter.out.persistence;

import com.zhangyuan.order.domain.model.Order;
import com.zhangyuan.order.domain.model.OrderNumber;
import com.zhangyuan.order.domain.repository.OrderRepository;
import com.zhangyuan.order.repository.OrderMainEntityRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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
    public Order save(Order order) {
        OrderMainEntity entity;
        if (order.getId() != null) {
            entity = jpaRepository.findById(order.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found for update: " + order.getId()));
            entity.setOrderNo(order.getOrderNo().value());
            entity.setPlanId(order.getPlanId());
            entity.setPriceId(order.getPriceId());
            entity.setAmount(order.getAmount());
            entity.setCurrency(order.getCurrency());
            entity.setStatus(order.getStatus().name().toLowerCase());
            entity.setSnapshotJson(order.getSnapshotJson());
            entity.setPaidAt(order.getPaidAt());
            entity.setFulfilledAt(order.getFulfilledAt());
            entity.setCancelledAt(order.getCancelledAt());
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
        OrderMainEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private Order toDomain(OrderMainEntity entity) {
        Order order = new Order(
                new OrderNumber(entity.getOrderNo()),
                entity.getPlanId(),
                entity.getPriceId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getSnapshotJson()
        );
        order.setId(entity.getId());
        order.setUserId(entity.getUserId());
        order.setCreatedAt(entity.getCreatedAt());
        order.setPaidAt(entity.getPaidAt());
        order.setFulfilledAt(entity.getFulfilledAt());
        order.setCancelledAt(entity.getCancelledAt());

        if ("fulfilled".equals(entity.getStatus())) {
            try { order.markFulfilled(); } catch (IllegalStateException ignored) {}
        } else if ("paid".equals(entity.getStatus())) {
            order.markPaid();
        } else if ("cancelled".equals(entity.getStatus())) {
            try { order.cancel(); } catch (IllegalStateException ignored) {}
        }

        return order;
    }
}
