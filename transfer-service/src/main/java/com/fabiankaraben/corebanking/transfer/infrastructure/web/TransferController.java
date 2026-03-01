package com.fabiankaraben.corebanking.transfer.infrastructure.web;

import com.fabiankaraben.corebanking.transfer.application.port.in.TransferUseCase;
import com.fabiankaraben.corebanking.transfer.domain.Money;
import com.fabiankaraben.corebanking.transfer.domain.TransferIntent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferUseCase transferUseCase;

    public TransferController(TransferUseCase transferUseCase) {
        this.transferUseCase = transferUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransferResponse initiateTransfer(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody TransferRequest request) {
        
        TransferIntent transfer = transferUseCase.initiateTransfer(
                idempotencyKey,
                request.sourceAccountId(),
                request.targetAccountId(),
                Money.of(request.amount())
        );

        return toResponse(transfer);
    }

    @GetMapping("/{id}")
    public TransferResponse getTransferStatus(@PathVariable UUID id) {
        TransferIntent transfer = transferUseCase.getTransferStatus(id);
        return toResponse(transfer);
    }

    private TransferResponse toResponse(TransferIntent transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccountId(),
                transfer.getTargetAccountId(),
                transfer.getAmount().amount().toPlainString(),
                transfer.getStatus().name(),
                transfer.getFailureReason(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt()
        );
    }

    public record TransferRequest(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {}
    public record TransferResponse(UUID id, UUID sourceAccountId, UUID targetAccountId, String amount, 
                                   String status, String failureReason, java.time.LocalDateTime createdAt, 
                                   java.time.LocalDateTime updatedAt) {}
}
