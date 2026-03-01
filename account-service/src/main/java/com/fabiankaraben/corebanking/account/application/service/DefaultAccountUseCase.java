package com.fabiankaraben.corebanking.account.application.service;

import com.fabiankaraben.corebanking.account.application.port.in.AccountUseCase;
import com.fabiankaraben.corebanking.account.application.port.out.AccountRepositoryPort;
import com.fabiankaraben.corebanking.account.domain.Account;
import com.fabiankaraben.corebanking.account.domain.Money;
import com.fabiankaraben.corebanking.account.domain.exception.AccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DefaultAccountUseCase implements AccountUseCase {

    private final AccountRepositoryPort accountRepository;

    public DefaultAccountUseCase(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(String customerId) {
        Account account = Account.create(customerId);
        return accountRepository.save(account);
    }

    @Override
    public Account getAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    public void deposit(UUID id, Money amount) {
        Account account = getAccount(id);
        account.deposit(amount);
        accountRepository.save(account);
    }

    @Override
    public void withdraw(UUID id, Money amount) {
        Account account = getAccount(id);
        account.withdraw(amount);
        accountRepository.save(account);
    }
}
