package com.zhangyuan.user.application.service;

import com.zhangyuan.user.adapter.out.persistence.BalanceTransactionEntity;
import com.zhangyuan.user.adapter.out.persistence.SaasUserEntity;
import com.zhangyuan.user.repository.BalanceTransactionRepository;
import com.zhangyuan.user.repository.SaasUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BalanceServiceTest {

    private final SaasUserRepository userRepository = mock(SaasUserRepository.class);
    private final BalanceTransactionRepository txRepo = mock(BalanceTransactionRepository.class);
    private BalanceService service;

    @BeforeEach
    void setUp() {
        service = new BalanceService(userRepository, txRepo);
    }

    private SaasUserEntity createUser(Long id, BigDecimal balance) {
        SaasUserEntity u = new SaasUserEntity("test@test.com", "hash", "Test");
        u.setId(id);
        u.setBalance(balance);
        u.setTotalRecharged(BigDecimal.ZERO);
        return u;
    }

    @Test
    void recharge_increasesBalance() {
        SaasUserEntity user = createUser(1L, BigDecimal.valueOf(100));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        service.recharge(1L, BigDecimal.valueOf(50), "测试充值");

        assertThat(user.getBalance()).isEqualByComparingTo("150");
        assertThat(user.getTotalRecharged()).isEqualByComparingTo("50");
        verify(txRepo).save(any());
    }

    @Test
    void deduct_decreasesBalance() {
        SaasUserEntity user = createUser(1L, BigDecimal.valueOf(100));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        service.deduct(1L, BigDecimal.valueOf(30), "测试扣费");

        assertThat(user.getBalance()).isEqualByComparingTo("70");
        verify(txRepo).save(any());
    }

    @Test
    void deduct_insufficientBalance_throws() {
        SaasUserEntity user = createUser(1L, BigDecimal.valueOf(10));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.deduct(1L, BigDecimal.valueOf(100), "超额扣费"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("余额不足");
    }

    @Test
    void getBalance_returnsCorrectAmount() {
        SaasUserEntity user = createUser(1L, BigDecimal.valueOf(200));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BigDecimal balance = service.getBalance(1L);

        assertThat(balance).isEqualByComparingTo("200");
    }

    @Test
    void recharge_userNotFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recharge(999L, BigDecimal.TEN, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deduct_userNotFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deduct(999L, BigDecimal.TEN, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getBalance_userNotFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBalance(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getTransactions_returnsList() {
        BalanceTransactionEntity tx1 = new BalanceTransactionEntity(1L, BigDecimal.valueOf(50), BigDecimal.valueOf(150), "RECHARGE", "充值");
        BalanceTransactionEntity tx2 = new BalanceTransactionEntity(1L, BigDecimal.valueOf(-30), BigDecimal.valueOf(120), "DEDUCT", "扣费");
        when(txRepo.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(tx1, tx2));

        List<BalanceTransactionEntity> result = service.getTransactions(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("50");
        assertThat(result.get(1).getAmount()).isEqualByComparingTo("-30");
    }

    @Test
    void getTransactions_emptyList() {
        when(txRepo.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<BalanceTransactionEntity> result = service.getTransactions(1L);

        assertThat(result).isEmpty();
    }
}
