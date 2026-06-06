package com.zhangyuan.user.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void createUser_setsInitialValues() {
        User user = new User("test@test.com", "hash", "Test");
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getNickname()).isEqualTo("Test");
        assertThat(user.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(user.getStatus()).isEqualTo("active");
        assertThat(user.getRole()).isEqualTo("user");
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void recharge_increasesBalance() {
        User user = new User("test@test.com", "hash", "Test");
        user.recharge(BigDecimal.valueOf(100));
        assertThat(user.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(user.getTotalRecharged()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void recharge_multipleIncrementsAccumulate() {
        User user = new User("test@test.com", "hash", "Test");
        user.recharge(BigDecimal.valueOf(50));
        user.recharge(BigDecimal.valueOf(30));
        assertThat(user.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(80));
        assertThat(user.getTotalRecharged()).isEqualByComparingTo(BigDecimal.valueOf(80));
    }

    @Test
    void deduct_reducesBalance() {
        User user = new User("test@test.com", "hash", "Test");
        user.recharge(BigDecimal.valueOf(100));
        user.deduct(BigDecimal.valueOf(30));
        assertThat(user.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(70));
    }

    @Test
    void deduct_whenInsufficientBalance_throws() {
        User user = new User("test@test.com", "hash", "Test");
        user.recharge(BigDecimal.valueOf(10));
        assertThatThrownBy(() -> user.deduct(BigDecimal.valueOf(100)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("余额不足");
    }

    @Test
    void deduct_exactBalance_succeeds() {
        User user = new User("test@test.com", "hash", "Test");
        user.recharge(BigDecimal.valueOf(50));
        user.deduct(BigDecimal.valueOf(50));
        assertThat(user.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void isActive_returnsFalse_whenStatusNotActive() {
        User user = new User("test@test.com", "hash", "Test");
        user.setStatus("disabled");
        assertThat(user.isActive()).isFalse();
    }
}
