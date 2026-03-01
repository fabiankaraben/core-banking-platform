package com.fabiankaraben.corebanking.transfer.domain.exception;

import java.util.UUID;

public class TransferNotFoundException extends RuntimeException {
    public TransferNotFoundException(UUID id) {
        super("Transfer not found with ID: " + id);
    }
}
