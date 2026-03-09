package com.cbp.transfer.adapter.in.web;

import com.cbp.transfer.adapter.in.web.dto.InitiateTransferRequest;
import com.cbp.transfer.adapter.in.web.dto.TransferResponse;
import com.cbp.transfer.domain.port.in.TransferUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST adapter (inbound port) for the Transfer Service.
 *
 * <p>Exposes the transfer use cases via a RESTful HTTP API under the base path
 * {@code /api/v1/transfers}. Every POST request must include an {@code Idempotency-Key}
 * header (UUID format recommended) to ensure safe replay semantics.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfers", description = "Money transfer operations — initiate and query transfers")
public class TransferController {

    private final TransferUseCase transferUseCase;

    /**
     * Constructs a {@code TransferController} with the required use case.
     *
     * @param transferUseCase the inbound port handling transfer operations
     */
    public TransferController(TransferUseCase transferUseCase) {
        this.transferUseCase = transferUseCase;
    }

    /**
     * Initiates a new money transfer between two accounts.
     *
     * <p>This endpoint is idempotent: submitting the same {@code Idempotency-Key}
     * multiple times returns the original response without creating duplicate transfers.
     *
     * @param idempotencyKey the client-supplied unique key for duplicate detection
     * @param request        the transfer initiation request body
     * @return HTTP {@code 202 Accepted} with the created transfer in {@code PENDING} status
     */
    @PostMapping
    @Operation(
            summary = "Initiate a money transfer",
            description = "Initiates a new asynchronous money transfer Saga. "
                    + "The transfer is recorded as PENDING immediately; the final outcome "
                    + "(COMPLETED or FAILED) is determined asynchronously by the Account Service "
                    + "via Kafka event choreography. "
                    + "Requires an **Idempotency-Key** header for safe replay."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Transfer accepted and Saga initiated",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or missing header"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<TransferResponse> initiateTransfer(
            @Parameter(description = "Client-supplied idempotency key (UUID format recommended)",
                       required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody InitiateTransferRequest request) {

        var transfer = transferUseCase.initiateTransfer(
                request.sourceAccountId(),
                request.targetAccountId(),
                request.amount(),
                request.currency(),
                request.description(),
                idempotencyKey
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(TransferResponse.from(transfer));
    }

    /**
     * Retrieves all transfers in the system.
     *
     * @return HTTP {@code 200 OK} with a list of all transfer representations
     */
    @GetMapping
    @Operation(summary = "List all transfers",
               description = "Returns all money transfers registered in the system.")
    @ApiResponse(responseCode = "200", description = "List of transfers returned")
    public ResponseEntity<List<TransferResponse>> getAllTransfers() {
        List<TransferResponse> transfers = transferUseCase.getAllTransfers()
                .stream()
                .map(TransferResponse::from)
                .toList();
        return ResponseEntity.ok(transfers);
    }

    /**
     * Retrieves a single transfer by its saga correlation UUID.
     *
     * @param transferId the UUID of the transfer to retrieve
     * @return HTTP {@code 200 OK} with the transfer, or {@code 404} if not found
     */
    @GetMapping("/{transferId}")
    @Operation(summary = "Get transfer by ID",
               description = "Retrieves the full status and details of a specific transfer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer found",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transfer not found")
    })
    public ResponseEntity<TransferResponse> getTransferById(
            @Parameter(description = "The transfer UUID") @PathVariable UUID transferId) {
        return ResponseEntity.ok(TransferResponse.from(transferUseCase.getTransferById(transferId)));
    }
}
