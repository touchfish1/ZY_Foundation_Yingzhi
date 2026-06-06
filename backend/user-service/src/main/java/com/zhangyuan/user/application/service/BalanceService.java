package com.zhangyuan.user.application.service;

import com.zhangyuan.user.domain.model.BalanceTransaction;
import com.zhangyuan.user.domain.model.User;
import com.zhangyuan.user.domain.repository.BalanceTransactionRepository;
import com.zhangyuan.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BalanceService {
    private static final Logger log = LoggerFactory.getLogger(BalanceService.class);
    private static final int MAX_RETRIES = 3;
    private final UserRepository userRepository;
    private final BalanceTransactionRepository txRepo;

    public BalanceService(UserRepository userRepository, BalanceTransactionRepository txRepo) {
        this.userRepository = userRepository;
        this.txRepo = txRepo;
    }

    @Transactional
    public User recharge(Long userId, BigDecimal amount, String desc) {
        return withRetry(() -> doRecharge(userId, amount, desc));
    }

    private User doRecharge(Long userId, BigDecimal amount, String desc) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.recharge(amount);
        userRepository.save(user);
        txRepo.save(new BalanceTransaction(userId, amount, user.getBalance(), "RECHARGE", desc));
        return user;
    }

    @Transactional
    public User deduct(Long userId, BigDecimal amount, String desc) {
        return withRetry(() -> doDeduct(userId, amount, desc));
    }

    private User doDeduct(Long userId, BigDecimal amount, String desc) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.deduct(amount);
        userRepository.save(user);
        txRepo.save(new BalanceTransaction(userId, amount.negate(), user.getBalance(), "DEDUCT", desc));
        return user;
    }

    private <T> T withRetry(java.util.function.Supplier<T> operation) {
        int attempts = 0;
        while (true) {
            try {
                return operation.get();
            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    log.error("Operation failed after {} retries due to optimistic lock", MAX_RETRIES, e);
                    throw e;
                }
                log.warn("Optimistic lock conflict, retrying ({}/{})", attempts, MAX_RETRIES);
            }
        }
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
