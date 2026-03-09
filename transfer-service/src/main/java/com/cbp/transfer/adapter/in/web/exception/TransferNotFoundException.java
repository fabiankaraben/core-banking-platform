package com.cbp.transfer.adapter.in.web.exception;

/**
 * Exception thrown when a transfer cannot be found by its identifier.
 *
 * <p>Translated by {@link GlobalExceptionHandler} into an HTTP {@code 404 Not Found} response.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public class TransferNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code TransferNotFoundException} with the given message.
     *
     * @param message a descriptive message identifying which transfer was not found
     */
    public TransferNotFoundException(String message) {
        super(message);
    }
}
