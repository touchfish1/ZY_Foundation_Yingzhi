package com.zhangyuan.order.repository;

import com.zhangyuan.order.adapter.out.persistence.OrderMainEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderMainEntityRepository extends JpaRepository<OrderMainEntity, Long> {

    Optional<OrderMainEntity> findByOrderNo(String orderNo);

    List<OrderMainEntity> findAllByOrderByCreatedAtDesc();

    List<OrderMainEntity> findByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(String status, Instant threshold, Pageable pageable);

    List<OrderMainEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
