package com.fabiankaraben.corebanking.transfer.application.port.in;

import com.fabiankaraben.corebanking.transfer.domain.Money;
import com.fabiankaraben.corebanking.transfer.domain.TransferIntent;

import java.util.UUID;

public interface TransferUseCase {
    TransferIntent initiateTransfer(String idempotencyKey, UUID sourceAccountId, UUID targetAccountId, Money amount);
    TransferIntent getTransferStatus(UUID id);
    void processTransferResult(UUID transferId, String status, String reason);
}
