package com.fabiankaraben.corebanking.notification.infrastructure.messaging;

import java.util.UUID;

public record TransferResultEvent(
        UUID transferId,
        String status, // "COMPLETED" or "FAILED"
        String reason
) {}
