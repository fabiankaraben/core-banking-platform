package com.cbp.account.domain.model;

/**
 * Domain exception thrown when a debit operation is attempted on an account
 * whose current balance is less than the requested amount.
 *
 * <p>This is a pure domain exception — it carries no HTTP or framework-specific
 * semantics. The adapter layer (e.g., {@code AccountController}) is responsible
 * for translating it into an appropriate HTTP response (typically {@code 422 Unprocessable Entity}).
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public class InsufficientFundsException extends RuntimeException {

    /**
     * Constructs a new {@code InsufficientFundsException} with the specified detail message.
     *
     * @param message a description of the insufficient-funds condition, including
     *                the available balance and requested amount for diagnostic purposes
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
}
