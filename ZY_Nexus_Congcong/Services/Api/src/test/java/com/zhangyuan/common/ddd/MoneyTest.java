package com.zhangyuan.common.ddd;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void createWithValidValues() {
        Money money = Money.of("29.99", "CNY");
        assertThat(money.amount()).isEqualByComparingTo("29.99");
        assertThat(money.currency()).isEqualTo("CNY");
    }

    @Test
    void createZero() {
        assertThat(Money.ZERO.amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void addSameCurrency() {
        assertThat(Money.of("10", "CNY").add(Money.of("20", "CNY")).amount()).isEqualByComparingTo("30.00");
    }

    @Test
    void addDifferentCurrencyThrows() {
        assertThatThrownBy(() -> Money.of("10", "CNY").add(Money.of("10", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isGreaterThan() {
        assertThat(Money.of("100", "CNY").isGreaterThan(Money.of("50", "CNY"))).isTrue();
    }

    @Test
    void nullAmountThrows() {
        assertThatThrownBy(() -> new Money(null, "CNY")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankCurrencyThrows() {
        assertThatThrownBy(() -> Money.of("10", "")).isInstanceOf(IllegalArgumentException.class);
    }
}
