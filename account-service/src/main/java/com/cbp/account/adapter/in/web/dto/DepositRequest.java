package com.cbp.account.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body DTO for depositing funds into an account.
 *
 * @param amount the amount to deposit; must be positive with at most 2 decimal places
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public record DepositRequest(

        @NotNull(message = "Amount must not be null")
        @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
        @Digits(integer = 13, fraction = 2,
                message = "Amount must have at most 13 integer digits and 2 decimal places")
        BigDecimal amount
) {
}
