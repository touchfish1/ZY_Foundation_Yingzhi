package com.zhangyuan.user.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.user.adapter.out.persistence.BalanceTransactionEntity;
import com.zhangyuan.user.adapter.out.persistence.SaasUserEntity;
import com.zhangyuan.user.dto.LoginRequest;
import com.zhangyuan.user.dto.RegisterRequest;
import com.zhangyuan.user.repository.BalanceTransactionRepository;
import com.zhangyuan.user.repository.SaasUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 完整业务流程测试:
 * 注册 -> 登录 -> 查余额 -> 充值 -> 扣费 -> 余额不足异常 -> 查流水
 */
class BusinessFlowTest {

    private final SaasUserRepository userRepo = mock(SaasUserRepository.class);
    private final BalanceTransactionRepository txRepo = mock(BalanceTransactionRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private SaasUserApplicationService userService;
    private BalanceService balanceService;
    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void setUp() {
        stpUtilMock = mockStatic(StpUtil.class);
        stpUtilMock.when(() -> StpUtil.login(anyString())).then(invocation -> null);
        stpUtilMock.when(StpUtil::getTokenValue).thenReturn("mock-token-value");

        userService = new SaasUserApplicationService(userRepo, passwordEncoder);
        balanceService = new BalanceService(userRepo, txRepo);
    }

    @AfterEach
    void tearDown() {
        if (stpUtilMock != null) {
            stpUtilMock.close();
        }
    }

    @Test
    void fullBusinessFlow() {
        // 1. 注册
        String email = "test@example.com";
        String password = "password123";
        String nickname = "TestUser";

        when(userRepo.existsByEmail(email)).thenReturn(false);
        when(userRepo.save(any(SaasUserEntity.class))).thenAnswer(i -> {
            SaasUserEntity u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        var loginResp = userService.register(new RegisterRequest(email, password, nickname));
        assertThat(loginResp.user().email()).isEqualTo(email);
        assertThat(loginResp.user().nickname()).isEqualTo(nickname);
        assertThat(loginResp.token()).isNotNull();

        // 2. 登录
        SaasUserEntity savedUser = new SaasUserEntity(email, passwordEncoder.encode(password), nickname);
        savedUser.setId(1L);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(savedUser));

        var loginResult = userService.login(new LoginRequest(email, password));
        assertThat(loginResult.user().email()).isEqualTo(email);

        // 3. 查余额（初始为0）
        when(userRepo.findById(1L)).thenReturn(Optional.of(savedUser));
        BigDecimal initialBalance = balanceService.getBalance(1L);
        assertThat(initialBalance).isEqualByComparingTo(BigDecimal.ZERO);

        // 4. 充值100
        doReturn(savedUser).when(userRepo).save(any(SaasUserEntity.class));
        balanceService.recharge(1L, BigDecimal.valueOf(100), "充值100元");
        assertThat(savedUser.getBalance()).isEqualByComparingTo("100");
        verify(txRepo, times(1)).save(any());

        // 5. 扣费30
        balanceService.deduct(1L, BigDecimal.valueOf(30), "购买套餐");
        assertThat(savedUser.getBalance()).isEqualByComparingTo("70");
        verify(txRepo, times(2)).save(any());

        // 6. 查流水
        balanceService.getTransactions(1L);
        verify(txRepo, times(1)).findByUserIdOrderByCreatedAtDesc(1L);

        // 7. 余额不足异常
        assertThatThrownBy(() -> balanceService.deduct(1L, BigDecimal.valueOf(200), "超额扣费"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("余额不足");
        assertThat(savedUser.getBalance()).isEqualByComparingTo("70"); // 余额未变
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepo.existsByEmail("existing@test.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.register(
            new RegisterRequest("existing@test.com", "pass123", "Existing")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already registered");
    }

    @Test
    void login_wrongPassword_throws() {
        SaasUserEntity user = new SaasUserEntity("test@test.com",
            passwordEncoder.encode("correct"), "Test");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> userService.login(new LoginRequest("test@test.com", "wrong")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid");
    }

    @Test
    void login_wrongEmail_throws() {
        when(userRepo.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.login(new LoginRequest("nonexistent@test.com", "pass")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid");
    }
}
