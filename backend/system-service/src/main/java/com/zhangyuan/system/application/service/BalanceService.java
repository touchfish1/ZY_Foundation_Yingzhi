package com.zhangyuan.system.application.service;

import com.zhangyuan.system.adapter.out.persistence.BalanceTransactionEntity;
import com.zhangyuan.system.adapter.out.persistence.SaasUserEntity;
import com.zhangyuan.system.repository.BalanceTransactionRepository;
import com.zhangyuan.system.repository.SaasUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BalanceService {
    private final SaasUserRepository userRepository;
    private final BalanceTransactionRepository txRepo;

    public BalanceService(SaasUserRepository userRepository, BalanceTransactionRepository txRepo) {
        this.userRepository = userRepository;
        this.txRepo = txRepo;
    }

    @Transactional
    public SaasUserEntity recharge(Long userId, BigDecimal amount, String desc) {
        SaasUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setBalance(user.getBalance().add(amount));
        user.setTotalRecharged(user.getTotalRecharged().add(amount));
        userRepository.save(user);
        txRepo.save(new BalanceTransactionEntity(userId, amount, user.getBalance(), "RECHARGE", desc));
        return user;
    }

    @Transactional
    public SaasUserEntity deduct(Long userId, BigDecimal amount, String desc) {
        SaasUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("余额不足");
        }
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        txRepo.save(new BalanceTransactionEntity(userId, amount.negate(), user.getBalance(), "DEDUCT", desc));
        return user;
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        return userRepository.findById(userId)
                .map(SaasUserEntity::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<BalanceTransactionEntity> getTransactions(Long userId) {
        return txRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
