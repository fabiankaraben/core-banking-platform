package com.cbp.account.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body DTO for creating a new bank account.
 *
 * <p>Validated via Bean Validation annotations before reaching the application layer.
 * Failures result in an HTTP {@code 400 Bad Request} response via
 * {@link com.cbp.account.adapter.in.web.exception.GlobalExceptionHandler}.
 *
 * @param ownerName full legal name of the account owner; must not be blank
 * @param currency  ISO-4217 three-letter currency code (e.g., {@code "USD"})
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public record CreateAccountRequest(

        @NotBlank(message = "Owner name must not be blank")
        @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
        String ownerName,

        @NotBlank(message = "Currency must not be blank")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO-4217 code (e.g. USD, EUR)")
        String currency
) {
}
