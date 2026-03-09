package com.cbp.transfer.adapter.in.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body DTO for initiating a new money transfer.
 *
 * <p>The client must also provide an {@code Idempotency-Key} HTTP header to ensure
 * safe replay semantics. Validated via Bean Validation before reaching the application layer.
 *
 * @param sourceAccountId UUID of the account to debit; must not be null
 * @param targetAccountId UUID of the account to credit; must not be null
 * @param amount          transfer amount; must be positive, max 2 decimal places
 * @param currency        ISO-4217 currency code (e.g., {@code "USD"})
 * @param description     optional human-readable transfer description
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public record InitiateTransferRequest(

        @NotNull(message = "Source account ID must not be null")
        UUID sourceAccountId,

        @NotNull(message = "Target account ID must not be null")
        UUID targetAccountId,

        @NotNull(message = "Amount must not be null")
        @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
        @Digits(integer = 13, fraction = 2,
                message = "Amount must have at most 13 integer digits and 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "Currency must not be blank")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO-4217 code (e.g. USD, EUR)")
        String currency,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {
}
