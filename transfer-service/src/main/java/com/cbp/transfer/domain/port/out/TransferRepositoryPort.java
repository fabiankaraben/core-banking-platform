package com.cbp.transfer.domain.port.out;

import com.cbp.transfer.domain.model.Transfer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary outbound port defining the persistence contract for {@link Transfer} entities.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface TransferRepositoryPort {

    /**
     * Persists a new or updated {@link Transfer} entity.
     *
     * @param transfer the transfer to save; must not be null
     * @return the saved transfer (with any auto-generated fields populated)
     */
    Transfer save(Transfer transfer);

    /**
     * Retrieves a transfer by its saga correlation UUID.
     *
     * @param id the UUID of the transfer to find
     * @return an {@link Optional} containing the transfer if found, or empty
     */
    Optional<Transfer> findById(UUID id);

    /**
     * Retrieves all transfers stored in the system.
     *
     * @return a list of all transfers; never null, but may be empty
     */
    List<Transfer> findAll();

    /**
     * Retrieves a transfer by its client-supplied idempotency key.
     *
     * @param idempotencyKey the idempotency key to look up
     * @return an {@link Optional} containing the transfer if found, or empty
     */
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
}
