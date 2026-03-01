package com.fabiankaraben.corebanking.account.domain.exception;

import com.fabiankaraben.corebanking.account.domain.Money;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(UUID accountId, Money currentBalance, Money requestedAmount) {
        super(String.format("Account %s has insufficient funds. Current balance: %s, Requested: %s",
                accountId, currentBalance.amount(), requestedAmount.amount()));
    }
}
