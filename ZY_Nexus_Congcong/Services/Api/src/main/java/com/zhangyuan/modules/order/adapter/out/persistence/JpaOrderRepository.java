package com.zhangyuan.modules.order.adapter.out.persistence;

import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import com.zhangyuan.modules.order.domain.repository.OrderRepository;
import com.zhangyuan.modules.order.repository.OrderMainRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaOrderRepository implements OrderRepository {

    private final OrderMainRepository jpaRepository;

    public JpaOrderRepository(OrderMainRepository jpaRepository) {
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
        com.zhangyuan.modules.order.domain.OrderMain entity = new com.zhangyuan.modules.order.domain.OrderMain(
                order.getOrderNo().value(),
                order.getPlanId(),
                order.getPriceId(),
                order.getAmount(),
                order.getCurrency(),
                order.getSnapshotJson()
        );
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private Order toDomain(com.zhangyuan.modules.order.domain.OrderMain entity) {
        Order order = new Order(
                new OrderNumber(entity.getOrderNo()),
                entity.getPlanId(),
                entity.getPriceId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getSnapshotJson()
        );
        if ("paid".equals(entity.getStatus())) {
            order.markPaid();
        }
        return order;
    }
}
