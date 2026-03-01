package com.fabiankaraben.corebanking.transfer.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRepositoryPort {
    Optional<UUID> getTransferId(String idempotencyKey);
    void saveTransferId(String idempotencyKey, UUID transferId);
}
