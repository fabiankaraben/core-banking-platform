package com.cbp.transfer.adapter.out.persistence;

import com.cbp.transfer.adapter.out.persistence.entity.TransferJpaEntity;
import com.cbp.transfer.adapter.out.persistence.repository.TransferJpaRepository;
import com.cbp.transfer.domain.model.Transfer;
import com.cbp.transfer.domain.port.out.TransferRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter implementing the {@link TransferRepositoryPort} outbound port.
 *
 * <p>Maps between the {@link Transfer} domain entity and the {@link TransferJpaEntity}
 * JPA entity. No domain or application code directly depends on this class.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Component
public class TransferPersistenceAdapter implements TransferRepositoryPort {

    private final TransferJpaRepository jpaRepository;

    /**
     * Constructs the persistence adapter with the required JPA repository.
     *
     * @param jpaRepository the Spring Data JPA repository for transfer entities
     */
    public TransferPersistenceAdapter(TransferJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /** {@inheritDoc} */
    @Override
    public Transfer save(Transfer transfer) {
        TransferJpaEntity entity = toJpaEntity(transfer);
        return toDomain(jpaRepository.save(entity));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Transfer> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public List<Transfer> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Transfer> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    // -------------------------------------------------------------------------
    // Private mapping helpers
    // -------------------------------------------------------------------------

    private TransferJpaEntity toJpaEntity(Transfer t) {
        return new TransferJpaEntity(
                t.getId(), t.getSourceAccountId(), t.getTargetAccountId(),
                t.getAmount(), t.getCurrency(), t.getStatus(), t.getDescription(),
                t.getIdempotencyKey(), t.getFailureReason(), t.getVersion(),
                t.getCreatedAt(), t.getUpdatedAt()
        );
    }

    private Transfer toDomain(TransferJpaEntity e) {
        return new Transfer(
                e.getId(), e.getSourceAccountId(), e.getTargetAccountId(),
                e.getAmount(), e.getCurrency(), e.getStatus(), e.getDescription(),
                e.getIdempotencyKey(), e.getFailureReason(), e.getVersion(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
