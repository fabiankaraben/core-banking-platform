package com.fabiankaraben.corebanking.transfer.infrastructure.persistence;

import com.fabiankaraben.corebanking.transfer.application.port.out.TransferRepositoryPort;
import com.fabiankaraben.corebanking.transfer.domain.Money;
import com.fabiankaraben.corebanking.transfer.domain.TransferIntent;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TransferPersistenceAdapter implements TransferRepositoryPort {

    private final SpringDataTransferRepository repository;

    public TransferPersistenceAdapter(SpringDataTransferRepository repository) {
        this.repository = repository;
    }

    @Override
    public TransferIntent save(TransferIntent transfer) {
        TransferEntity entity = new TransferEntity(
                transfer.getId(),
                transfer.getSourceAccountId(),
                transfer.getTargetAccountId(),
                transfer.getAmount().amount(),
                transfer.getStatus().name(),
                transfer.getFailureReason(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt());
        TransferEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TransferIntent> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private TransferIntent toDomain(TransferEntity entity) {
        return new TransferIntent(
                entity.getId(),
                entity.getSourceAccountId(),
                entity.getTargetAccountId(),
                Money.of(entity.getAmount()),
                com.fabiankaraben.corebanking.transfer.domain.TransferStatus.valueOf(entity.getStatus()),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
