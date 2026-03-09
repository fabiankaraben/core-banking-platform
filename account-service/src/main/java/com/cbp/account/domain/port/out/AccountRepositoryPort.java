package com.cbp.account.domain.port.out;

import com.cbp.account.domain.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary outbound port defining the persistence contract for {@link Account} entities.
 *
 * <p>The domain and application layers depend on this interface, not on any concrete
 * JPA repository. This allows the persistence technology to be swapped (e.g., from
 * PostgreSQL to MongoDB) without touching any domain or application code.
 *
 * <p>The concrete implementation lives in the persistence adapter:
 * {@code AccountPersistenceAdapter}.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface AccountRepositoryPort {

    /**
     * Persists a new or updated {@link Account} entity.
     *
     * @param account the account to save; must not be null
     * @return the saved account (potentially with auto-generated fields populated)
     */
    Account save(Account account);

    /**
     * Retrieves an account by its unique identifier.
     *
     * @param id the UUID of the account to find
     * @return an {@link Optional} containing the account if found, or empty if not
     */
    Optional<Account> findById(UUID id);

    /**
     * Retrieves all accounts stored in the system.
     *
     * @return a list of all accounts; never null, but may be empty
     */
    List<Account> findAll();

    /**
     * Checks whether an account with the given account number already exists.
     *
     * @param accountNumber the account number to check
     * @return {@code true} if an account with this number exists, {@code false} otherwise
     */
    boolean existsByAccountNumber(String accountNumber);
}
