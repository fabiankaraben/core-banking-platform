package com.fabiankaraben.corebanking.account.infrastructure.persistence;

import com.fabiankaraben.corebanking.account.application.port.out.AccountRepositoryPort;
import com.fabiankaraben.corebanking.account.domain.Account;
import com.fabiankaraben.corebanking.account.domain.Money;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository jpaRepository;

    public AccountPersistenceAdapter(SpringDataAccountRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = toEntity(account);
        AccountEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private AccountEntity toEntity(Account account) {
        return new AccountEntity(
                account.getId(),
                account.getCustomerId(),
                account.getBalance().amount(),
                account.getVersion(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private Account toDomain(AccountEntity entity) {
        return new Account(
                entity.getId(),
                entity.getCustomerId(),
                Money.of(entity.getBalance()),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
