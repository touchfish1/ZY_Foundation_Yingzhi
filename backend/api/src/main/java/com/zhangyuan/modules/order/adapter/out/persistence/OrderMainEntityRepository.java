package com.zhangyuan.modules.order.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderMainEntityRepository extends JpaRepository<OrderMainEntity, Long> {

    Optional<OrderMainEntity> findByOrderNo(String orderNo);

    List<OrderMainEntity> findAllByOrderByCreatedAtDesc();
}
