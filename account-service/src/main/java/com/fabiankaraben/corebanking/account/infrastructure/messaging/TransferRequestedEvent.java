package com.fabiankaraben.corebanking.account.infrastructure.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestedEvent(
        UUID transferId,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount
) {}
