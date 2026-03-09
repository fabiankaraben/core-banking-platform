package com.cbp.transfer.adapter.in.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Centralized exception handler for the Transfer Service REST layer.
 *
 * <p>Translates domain and application exceptions into RFC 7807 Problem Detail
 * responses with a consistent structure across all endpoints.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link TransferNotFoundException} — returns HTTP 404.
     *
     * @param ex the caught exception
     * @return a {@link ProblemDetail} with type, title, and detail fields populated
     */
    @ExceptionHandler(TransferNotFoundException.class)
    public ProblemDetail handleTransferNotFound(TransferNotFoundException ex) {
        log.warn("Transfer not found: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://cbp.com/errors/transfer-not-found"));
        pd.setTitle("Transfer Not Found");
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    /**
     * Handles {@link IllegalArgumentException} (e.g., invalid amount) — returns HTTP 400.
     *
     * @param ex the caught exception
     * @return a {@link ProblemDetail} with the validation message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("https://cbp.com/errors/invalid-request"));
        pd.setTitle("Invalid Request");
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    /**
     * Handles missing required HTTP headers (e.g., {@code Idempotency-Key}) — returns HTTP 400.
     *
     * @param ex the caught exception
     * @return a {@link ProblemDetail} indicating which header is missing
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex) {
        log.warn("Missing header: {}", ex.getHeaderName());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Required request header '" + ex.getHeaderName() + "' is missing.");
        pd.setType(URI.create("https://cbp.com/errors/missing-header"));
        pd.setTitle("Missing Required Header");
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    /**
     * Handles bean validation failures — returns HTTP 400.
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
