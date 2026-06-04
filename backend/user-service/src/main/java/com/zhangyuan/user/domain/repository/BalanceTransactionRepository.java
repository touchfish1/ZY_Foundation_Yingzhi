package com.zhangyuan.user.domain.repository;

import com.zhangyuan.user.domain.model.BalanceTransaction;

import java.util.List;

public interface BalanceTransactionRepository {
    BalanceTransaction save(BalanceTransaction transaction);
    List<BalanceTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
