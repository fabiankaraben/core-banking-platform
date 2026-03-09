package com.cbp.account.adapter.in.web.exception;

/**
 * Exception thrown when an account cannot be found by its identifier.
 *
 * <p>Used by the application layer and translated by the
 * {@link GlobalExceptionHandler} into an HTTP {@code 404 Not Found} response.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public class AccountNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code AccountNotFoundException} with the given message.
     *
     * @param message a descriptive message identifying which account was not found
     */
    public AccountNotFoundException(String message) {
        super(message);
    }
}
