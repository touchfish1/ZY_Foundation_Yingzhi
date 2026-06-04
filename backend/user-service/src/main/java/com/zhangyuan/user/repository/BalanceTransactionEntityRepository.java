package com.zhangyuan.user.repository;

import com.zhangyuan.user.adapter.out.persistence.BalanceTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceTransactionEntityRepository extends JpaRepository<BalanceTransactionEntity, Long> {
    List<BalanceTransactionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
