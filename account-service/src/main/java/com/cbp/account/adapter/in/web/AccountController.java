package com.cbp.account.adapter.in.web;

import com.cbp.account.adapter.in.web.dto.AccountResponse;
import com.cbp.account.adapter.in.web.dto.CreateAccountRequest;
import com.cbp.account.adapter.in.web.dto.DepositRequest;
import com.cbp.account.domain.port.in.AccountUseCase;
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
 * REST adapter (inbound port) for the Account Service.
 *
 * <p>Exposes the account management use cases via a RESTful HTTP API. All requests
 * are validated against the declared Bean Validation constraints and routed to the
 * {@link AccountUseCase} inbound port. Exceptions are translated to RFC 7807
 * Problem Detail responses by {@link com.cbp.account.adapter.in.web.exception.GlobalExceptionHandler}.
 *
 * <p>Base path: {@code /api/v1/accounts}
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Account lifecycle management — create, query, and fund accounts")
public class AccountController {

    private final AccountUseCase accountUseCase;

    /**
     * Constructs an {@code AccountController} with the required use case.
     *
     * @param accountUseCase the inbound port handling account operations
     */
    public AccountController(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }

    /**
     * Creates a new bank account.
     *
     * <p>The account is immediately activated and ready for transactions upon creation.
     *
     * @param request the account creation request containing owner name and currency
     * @return HTTP {@code 201 Created} with the newly created account in the response body
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new account",
               description = "Creates a new bank account for the specified owner and currency. "
                       + "The account is immediately activated upon creation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        var account = accountUseCase.createAccount(request.ownerName(), request.currency());
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(account));
    }

    /**
     * Retrieves all accounts in the system.
     *
     * @return HTTP {@code 200 OK} with a list of all account representations
     */
    @GetMapping
    @Operation(summary = "List all accounts",
               description = "Returns all bank accounts registered in the system.")
    @ApiResponse(responseCode = "200", description = "List of accounts returned")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts = accountUseCase.getAllAccounts()
                .stream()
                .map(AccountResponse::from)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Retrieves a single account by its unique identifier.
     *
     * @param accountId the UUID of the account to retrieve
     * @return HTTP {@code 200 OK} with the account, or {@code 404 Not Found} if absent
     */
    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID",
               description = "Retrieves the full account details for the given account UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountResponse> getAccountById(
            @Parameter(description = "The account UUID") @PathVariable UUID accountId) {
        return ResponseEntity.ok(AccountResponse.from(accountUseCase.getAccountById(accountId)));
    }

    /**
     * Deposits funds into an account.
     *
     * <p>The account must be in {@link com.cbp.account.domain.model.AccountStatus#ACTIVE} status.
     *
     * @param accountId the UUID of the target account
     * @param request   the deposit request containing the amount to credit
     * @return HTTP {@code 200 OK} with the updated account balance
     */
    @PostMapping("/{accountId}/deposit")
    @Operation(summary = "Deposit funds",
               description = "Credits the specified amount to an active account. "
                       + "Banker's Rounding (HALF_EVEN) is applied.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit applied successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "409", description = "Account is not in ACTIVE state"),
            @ApiResponse(responseCode = "400", description = "Invalid amount")
    })
    public ResponseEntity<AccountResponse> deposit(
            @Parameter(description = "The account UUID") @PathVariable UUID accountId,
            @Valid @RequestBody DepositRequest request) {
        var account = accountUseCase.deposit(accountId, request.amount());
        return ResponseEntity.ok(AccountResponse.from(account));
    }
}
