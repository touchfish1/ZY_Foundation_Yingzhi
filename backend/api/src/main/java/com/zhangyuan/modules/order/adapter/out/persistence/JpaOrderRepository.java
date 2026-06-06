package com.zhangyuan.modules.order.adapter.out.persistence;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.order.domain.model.Order;
import com.zhangyuan.modules.order.domain.model.OrderNumber;
import com.zhangyuan.modules.order.domain.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public PageResponse<Order> findPageByCreatedAtDesc(int page, int pageSize) {
        Page<OrderMainEntity> pageResult = jpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, pageSize));
        List<Order> items = pageResult.getContent().stream()
                .map(this::toDomain)
                .toList();
        return PageResponse.of(items, page, pageSize, pageResult.getTotalElements());
    }

    @Override
    public Order save(Order order) {
        OrderMainEntity entity = new OrderMainEntity(
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

    private Order toDomain(OrderMainEntity entity) {
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
