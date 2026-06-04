package com.zhangyuan.order.repository;

import com.zhangyuan.order.adapter.out.persistence.OrderMainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderMainEntityRepository extends JpaRepository<OrderMainEntity, Long> {

    Optional<OrderMainEntity> findByOrderNo(String orderNo);

    List<OrderMainEntity> findAllByOrderByCreatedAtDesc();
}
