package com.fabiankaraben.corebanking.transfer.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) {
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive in a transfer");
        }
        amount = amount.setScale(4, RoundingMode.HALF_EVEN);
    }
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }
}
