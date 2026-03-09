package com.cbp.transfer.application.service;

import com.cbp.transfer.adapter.in.web.exception.TransferNotFoundException;
import com.cbp.transfer.domain.event.TransferRequestedEvent;
import com.cbp.transfer.domain.model.Transfer;
import com.cbp.transfer.domain.port.in.TransferUseCase;
import com.cbp.transfer.domain.port.out.EventPublisherPort;
import com.cbp.transfer.domain.port.out.IdempotencyPort;
import com.cbp.transfer.domain.port.out.TransferRepositoryPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing the {@link TransferUseCase} inbound port.
 *
 * <p>Orchestrates the full transfer initiation flow:
 * <ol>
 *   <li>Checks Redis for a cached response matching the {@code idempotencyKey}.</li>
 *   <li>Validates the incoming request.</li>
 *   <li>Persists the {@link Transfer} record in {@code PENDING} status.</li>
 *   <li>Publishes a {@link TransferRequestedEvent} to Kafka within the same transaction
 *       (Transactional Outbox pattern).</li>
 *   <li>Caches the response in Redis for 24 hours for idempotent replay.</li>
 * </ol>
 *
 * <p>Also handles Saga completion callbacks: {@link #markTransferCompleted(UUID)}
 * and {@link #markTransferFailed(UUID, String)} update the persisted transfer state
 * when Kafka outcome events arrive.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Service
@Transactional
public class TransferService implements TransferUseCase {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransferRepositoryPort transferRepository;
    private final EventPublisherPort eventPublisher;
    private final IdempotencyPort idempotency;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a {@code TransferService} with its required dependencies.
     *
     * @param transferRepository the persistence port for loading and saving transfers
     * @param eventPublisher     the event publishing port for initiating the Saga
     * @param idempotency        the Redis-backed idempotency port
     * @param objectMapper       the Jackson mapper for idempotency response serialization
     */
    public TransferService(TransferRepositoryPort transferRepository,
                           EventPublisherPort eventPublisher,
                           IdempotencyPort idempotency,
                           ObjectMapper objectMapper) {
        this.transferRepository = transferRepository;
        this.eventPublisher = eventPublisher;
        this.idempotency = idempotency;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the {@code idempotencyKey} matches a previous request stored in Redis,
     * deserializes and returns the cached {@link Transfer} without any side effects.
     */
    @Override
    public Transfer initiateTransfer(UUID sourceAccountId, UUID targetAccountId,
                                     BigDecimal amount, String currency,
                                     String description, String idempotencyKey) {
        log.info("Initiating transfer: idempotencyKey={}, from={}, to={}, amount={}",
                idempotencyKey, sourceAccountId, targetAccountId, amount);

        // 1. Idempotency check — return cached response if key already seen
        var existing = transferRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent replay: returning existing transfer id={}", existing.get().getId());
            return existing.get();
        }

        // 2. Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive, got: " + amount);
        }
        if (sourceAccountId.equals(targetAccountId)) {
            throw new IllegalArgumentException("Source and target accounts must be different.");
        }

        // 3. Persist transfer in PENDING state
        Transfer transfer = new Transfer(sourceAccountId, targetAccountId, amount,
                currency, description, idempotencyKey);
        Transfer saved = transferRepository.save(transfer);

        // 4. Publish TransferRequestedEvent to initiate the Saga
        TransferRequestedEvent event = TransferRequestedEvent.of(
                saved.getId(), sourceAccountId, targetAccountId, amount, currency);
        eventPublisher.publish(event);

        // 5. Cache in Redis for idempotent replay
        try {
            idempotency.store(idempotencyKey, objectMapper.writeValueAsString(saved));
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache idempotency response for key={}: {}", idempotencyKey, e.getMessage());
        }

        log.info("Transfer initiated: id={}, status={}", saved.getId(), saved.getStatus());
        return saved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Transfer getTransferById(UUID transferId) {
        return transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(
                        "Transfer not found with id: " + transferId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markTransferCompleted(UUID transferId) {
        log.info("Marking transfer COMPLETED: id={}", transferId);
        transferRepository.findById(transferId).ifPresentOrElse(transfer -> {
            transfer.complete();
            transferRepository.save(transfer);
        }, () -> log.warn("Received TransferCompletedEvent for unknown transferId={}", transferId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markTransferFailed(UUID transferId, String failureReason) {
        log.info("Marking transfer FAILED: id={}, reason={}", transferId, failureReason);
        transferRepository.findById(transferId).ifPresentOrElse(transfer -> {
            transfer.fail(failureReason);
            transferRepository.save(transfer);
        }, () -> log.warn("Received TransferFailedEvent for unknown transferId={}", transferId));
    }
}
