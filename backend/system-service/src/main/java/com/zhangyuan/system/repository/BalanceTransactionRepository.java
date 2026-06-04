package com.zhangyuan.system.repository;

import com.zhangyuan.system.adapter.out.persistence.BalanceTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceTransactionRepository extends JpaRepository<BalanceTransactionEntity, Long> {
    List<BalanceTransactionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
