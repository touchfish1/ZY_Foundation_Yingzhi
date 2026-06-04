package com.zhangyuan.user.adapter.out.persistence;

import com.zhangyuan.user.domain.model.BalanceTransaction;
import com.zhangyuan.user.domain.repository.BalanceTransactionRepository;
import com.zhangyuan.user.repository.BalanceTransactionEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaBalanceTransactionAdapter implements BalanceTransactionRepository {

    private final BalanceTransactionEntityRepository repo;

    public JpaBalanceTransactionAdapter(BalanceTransactionEntityRepository repo) {
        this.repo = repo;
    }

    @Override
    public BalanceTransaction save(BalanceTransaction transaction) {
        BalanceTransactionEntity entity = toEntity(transaction);
        BalanceTransactionEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<BalanceTransaction> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    private BalanceTransaction toDomain(BalanceTransactionEntity e) {
        BalanceTransaction bt = new BalanceTransaction(
                e.getUserId(), e.getAmount(), e.getBalanceAfter(),
                e.getTransactionType(), e.getDescription()
        );
        bt.setId(e.getId());
        bt.setCreatedAt(e.getCreatedAt());
        return bt;
    }

    private BalanceTransactionEntity toEntity(BalanceTransaction bt) {
        return new BalanceTransactionEntity(
                bt.getUserId(), bt.getAmount(), bt.getBalanceAfter(),
                bt.getTransactionType(), bt.getDescription()
        );
    }
}
