package com.cbp.account.adapter.in.web.exception;

import com.cbp.account.domain.model.InsufficientFundsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Centralized exception handler for the Account Service REST layer.
 *
 * <p>Translates domain and application exceptions into RFC 7807 Problem Detail
 * responses, providing machine-readable error payloads with a consistent structure
 * across all endpoints.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7807">RFC 7807 — Problem Details for HTTP APIs</a>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link AccountNotFoundException} and returns HTTP 404.
     *
     * @param ex the caught exception
     * @return a {@link ProblemDetail} with type, title, and detail fields populated
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException ex) {
        log.warn("Account not found: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://cbp.com/errors/account-not-found"));
        pd.setTitle("Account Not Found");
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    /**
     * Handles {@link InsufficientFundsException} and returns HTTP 422.
     *
     * @param ex the caught exception
     * @return a {@link ProblemDetail} describing the insufficient-funds condition
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("https://cbp.com/errors/insufficient-funds"));
        pd.setTitle("Insufficient Funds");
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    /**
     * Handles {@link IllegalStateException} (e.g., operations on frozen/closed accounts)
     * and returns HTTP 409 Conflict.
     *
     * @param ex the caught exception
     * @return a {@link ProblemDetail} describing the illegal state
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://cbp.com/errors/invalid-account-state"));
        pd.setTitle("Invalid Account State");
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    /**
     * Handles bean validation failures on request bodies and returns HTTP 400.
     *
     * @param ex the caught validation exception
     * @return a {@link ProblemDetail} listing all field validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Request validation failed");
        pd.setType(URI.create("https://cbp.com/errors/validation-error"));
        pd.setTitle("Validation Error");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList());
        return pd;
    }
}
