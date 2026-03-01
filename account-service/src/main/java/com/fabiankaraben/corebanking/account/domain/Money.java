package com.fabiankaraben.corebanking.account.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) {

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        amount = amount.setScale(4, RoundingMode.HALF_EVEN);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money money) {
        return new Money(this.amount.add(money.amount()));
    }

    public Money subtract(Money money) {
        return new Money(this.amount.subtract(money.amount()));
    }

    public boolean isGreaterThanOrEqual(Money money) {
        return this.amount.compareTo(money.amount()) >= 0;
    }

    public boolean isLessThan(Money money) {
        return this.amount.compareTo(money.amount()) < 0;
    }
}
