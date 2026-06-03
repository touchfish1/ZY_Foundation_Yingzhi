package com.zhangyuan.common.dddframework;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount, String currency) implements ValueObject {

    public static final Money ZERO = new Money(BigDecimal.ZERO, "CNY");

    public Money {
        if (amount == null) throw new IllegalArgumentException("Amount must not be null");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency must not be blank");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money of(double amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }
}

