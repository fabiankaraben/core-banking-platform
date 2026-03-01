package com.fabiankaraben.corebanking.account.application.port.in;

import com.fabiankaraben.corebanking.account.domain.Account;
import com.fabiankaraben.corebanking.account.domain.Money;
import java.util.UUID;

public interface AccountUseCase {
    Account createAccount(String customerId);
    Account getAccount(UUID id);
    void deposit(UUID id, Money amount);
    void withdraw(UUID id, Money amount);
}
