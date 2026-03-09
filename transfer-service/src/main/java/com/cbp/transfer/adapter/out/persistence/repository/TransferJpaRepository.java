package com.cbp.transfer.adapter.out.persistence.repository;

import com.cbp.transfer.adapter.out.persistence.entity.TransferJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link TransferJpaEntity}.
 *
 * <p>This interface is an infrastructure detail referenced only by
 * {@link com.cbp.transfer.adapter.out.persistence.TransferPersistenceAdapter}.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface TransferJpaRepository extends JpaRepository<TransferJpaEntity, UUID> {

    /**
     * Finds a transfer entity by its idempotency key.
     *
     * @param idempotencyKey the client-supplied idempotency key
     * @return an {@link Optional} containing the entity if found
     */
    Optional<TransferJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
