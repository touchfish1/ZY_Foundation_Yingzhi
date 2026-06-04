package com.zhangyuan.user.application.service;

import com.zhangyuan.user.domain.model.BalanceTransaction;
import com.zhangyuan.user.domain.model.User;
import com.zhangyuan.user.domain.repository.BalanceTransactionRepository;
import com.zhangyuan.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BalanceService {
    private final UserRepository userRepository;
    private final BalanceTransactionRepository txRepo;

    public BalanceService(UserRepository userRepository, BalanceTransactionRepository txRepo) {
        this.userRepository = userRepository;
        this.txRepo = txRepo;
    }

    @Transactional
    public User recharge(Long userId, BigDecimal amount, String desc) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.recharge(amount);
        userRepository.save(user);
        txRepo.save(new BalanceTransaction(userId, amount, user.getBalance(), "RECHARGE", desc));
        return user;
    }

    @Transactional
    public User deduct(Long userId, BigDecimal amount, String desc) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.deduct(amount);
        userRepository.save(user);
        txRepo.save(new BalanceTransaction(userId, amount.negate(), user.getBalance(), "DEDUCT", desc));
        return user;
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        return userRepository.findById(userId)
                .map(User::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<BalanceTransaction> getTransactions(Long userId) {
        return txRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
