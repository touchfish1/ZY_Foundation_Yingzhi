package com.zhangyuan.modules.order.repository;

import com.zhangyuan.modules.order.domain.OrderMain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderMainRepository extends JpaRepository<OrderMain, Long> {

    Optional<OrderMain> findByOrderNo(String orderNo);

    List<OrderMain> findAllByOrderByCreatedAtDesc();
}
