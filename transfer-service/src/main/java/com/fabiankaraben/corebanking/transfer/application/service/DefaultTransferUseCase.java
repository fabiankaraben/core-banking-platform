package com.fabiankaraben.corebanking.transfer.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fabiankaraben.corebanking.transfer.application.port.in.TransferUseCase;
import com.fabiankaraben.corebanking.transfer.application.port.out.IdempotencyRepositoryPort;
import com.fabiankaraben.corebanking.transfer.application.port.out.OutboxRepositoryPort;
import com.fabiankaraben.corebanking.transfer.application.port.out.TransferRepositoryPort;
import com.fabiankaraben.corebanking.transfer.domain.Money;
import com.fabiankaraben.corebanking.transfer.domain.TransferIntent;
import com.fabiankaraben.corebanking.transfer.domain.exception.TransferNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DefaultTransferUseCase implements TransferUseCase {

    private final TransferRepositoryPort transferRepository;
    private final OutboxRepositoryPort outboxRepository;
    private final IdempotencyRepositoryPort idempotencyRepository;
    private final ObjectMapper objectMapper;

    public DefaultTransferUseCase(TransferRepositoryPort transferRepository,
            OutboxRepositoryPort outboxRepository,
            IdempotencyRepositoryPort idempotencyRepository,
            ObjectMapper objectMapper) {
        this.transferRepository = transferRepository;
        this.outboxRepository = outboxRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public TransferIntent initiateTransfer(String idempotencyKey, UUID sourceAccountId, UUID targetAccountId,
            Money amount) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<UUID> existingTransferId = idempotencyRepository.getTransferId(idempotencyKey);
            if (existingTransferId.isPresent()) {
                return getTransferStatus(existingTransferId.get());
            }
        }

        TransferIntent transfer = TransferIntent.create(sourceAccountId, targetAccountId, amount);
        transferRepository.save(transfer);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyRepository.saveTransferId(idempotencyKey, transfer.getId());
        }

        try {
            TransferRequestedEvent event = new TransferRequestedEvent(
                    transfer.getId(),
                    transfer.getSourceAccountId(),
                    transfer.getTargetAccountId(),
                    transfer.getAmount().amount());
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save("TransferIntent", transfer.getId(), "transfer-requested-topic", payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize transfer requested event", e);
        }

        return transfer;
    }

    @Override
    public TransferIntent getTransferStatus(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException(id));
    }

    @Override
    public void processTransferResult(UUID transferId, String status, String reason) {
        TransferIntent transfer = getTransferStatus(transferId);
        if ("COMPLETED".equals(status)) {
            transfer.markCompleted();
        } else if ("FAILED".equals(status)) {
            transfer.markFailed(reason);
        }
        transferRepository.save(transfer);
    }
}
