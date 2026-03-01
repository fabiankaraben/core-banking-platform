package com.fabiankaraben.corebanking.transfer.application.port.out;

import com.fabiankaraben.corebanking.transfer.domain.TransferIntent;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepositoryPort {
    TransferIntent save(TransferIntent transfer);
    Optional<TransferIntent> findById(UUID id);
}
