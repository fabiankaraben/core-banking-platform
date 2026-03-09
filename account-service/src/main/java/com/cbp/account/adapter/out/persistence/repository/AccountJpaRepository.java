package com.cbp.account.adapter.out.persistence.repository;

import com.cbp.account.adapter.out.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link AccountJpaEntity}.
 *
 * <p>This interface is an infrastructure detail — it is only referenced by the
 * persistence adapter ({@link com.cbp.account.adapter.out.persistence.AccountPersistenceAdapter})
 * and must never be injected directly into the application or domain layers.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    /**
     * Checks whether an account entity with the given account number already exists.
     *
     * @param accountNumber the account number to query
     * @return {@code true} if a record with this number exists, {@code false} otherwise
     */
    boolean existsByAccountNumber(String accountNumber);
}
