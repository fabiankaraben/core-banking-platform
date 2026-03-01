package com.fabiankaraben.corebanking.account.infrastructure.web;

import com.fabiankaraben.corebanking.account.application.port.in.AccountUseCase;
import com.fabiankaraben.corebanking.account.domain.Account;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountUseCase accountUseCase;

    public AccountController(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        Account account = accountUseCase.createAccount(request.customerId());
        return toResponse(account);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable UUID id) {
        Account account = accountUseCase.getAccount(id);
        return toResponse(account);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerId(),
                account.getBalance().amount().toPlainString(),
                account.getCreatedAt()
        );
    }

    public record CreateAccountRequest(String customerId) {}
    public record AccountResponse(UUID id, String customerId, String balance, java.time.LocalDateTime createdAt) {}
}
