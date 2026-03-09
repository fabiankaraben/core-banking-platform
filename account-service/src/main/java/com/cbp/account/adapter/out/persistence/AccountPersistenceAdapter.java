package com.cbp.account.adapter.out.persistence;

import com.cbp.account.adapter.out.persistence.entity.AccountJpaEntity;
import com.cbp.account.adapter.out.persistence.repository.AccountJpaRepository;
import com.cbp.account.domain.model.Account;
import com.cbp.account.domain.port.out.AccountRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter implementing the {@link AccountRepositoryPort} outbound port.
 *
 * <p>Bridges the domain layer and the Spring Data JPA infrastructure by mapping
 * between the {@link Account} domain entity and the {@link AccountJpaEntity} JPA entity.
 * No domain or application code directly depends on this class.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Component
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository jpaRepository;

    /**
     * Constructs the persistence adapter with the required JPA repository.
     *
     * @param jpaRepository the Spring Data JPA repository for account entities
     */
    public AccountPersistenceAdapter(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Maps the {@link Account} domain entity to a {@link AccountJpaEntity},
     * persists it via Spring Data JPA, and maps the result back to the domain model.
     */
    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = toJpaEntity(account);
        AccountJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Account> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return jpaRepository.existsByAccountNumber(accountNumber);
    }

    // -------------------------------------------------------------------------
    // Private mapping helpers
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link Account} domain entity to a {@link AccountJpaEntity}.
     *
     * @param account the domain entity to map
     * @return the corresponding JPA entity
     */
    private AccountJpaEntity toJpaEntity(Account account) {
        return new AccountJpaEntity(
                account.getId(),
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getVersion(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    /**
     * Maps a {@link AccountJpaEntity} to an {@link Account} domain entity.
     *
     * @param entity the JPA entity to map
     * @return the corresponding domain entity
     */
    private Account toDomain(AccountJpaEntity entity) {
        return new Account(
                entity.getId(),
                entity.getAccountNumber(),
                entity.getOwnerName(),
                entity.getBalance(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
