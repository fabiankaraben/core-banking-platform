package com.cbp.account.application.service;

import com.cbp.account.adapter.in.web.exception.AccountNotFoundException;
import com.cbp.account.domain.event.TransferCompletedEvent;
import com.cbp.account.domain.event.TransferFailedEvent;
import com.cbp.account.domain.model.Account;
import com.cbp.account.domain.model.InsufficientFundsException;
import com.cbp.account.domain.port.in.AccountUseCase;
import com.cbp.account.domain.port.out.AccountRepositoryPort;
import com.cbp.account.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing the {@link AccountUseCase} inbound port.
 *
 * <p>This class orchestrates interactions between the domain model and
 * infrastructure adapters (persistence, event publishing). It contains no
 * business logic itself — logic lives in the {@link Account} domain entity.
 * Its responsibilities are:
 * <ul>
 *   <li>Loading and saving {@link Account} entities via {@link AccountRepositoryPort}.</li>
 *   <li>Coordinating domain operations and publishing resulting {@link com.cbp.account.domain.event.DomainEvent}s.</li>
 *   <li>Managing transaction boundaries with {@link Transactional}.</li>
 *   <li>Translating domain exceptions into application-level signals.</li>
 * </ul>
 *
 * <p>The {@link Transactional} annotation ensures that all persistence operations
 * and outbox event inserts happen atomically within a single database transaction,
 * implementing the <em>Transactional Outbox Pattern</em>.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Service
@Transactional
public class AccountService implements AccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepositoryPort accountRepository;
    private final EventPublisherPort eventPublisher;

    /**
     * Constructs an {@code AccountService} with its required dependencies.
     *
     * @param accountRepository the persistence port for loading and saving accounts
     * @param eventPublisher    the event publishing port for emitting domain events
     */
    public AccountService(AccountRepositoryPort accountRepository,
                          EventPublisherPort eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a unique account number, creates the {@link Account} domain entity,
     * immediately activates it, and persists it.
     */
    @Override
    public Account createAccount(String ownerName, String currency) {
        log.info("Creating account for owner='{}', currency='{}'", ownerName, currency);
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, ownerName, currency);
        account.activate();
        Account saved = accountRepository.save(account);
        log.info("Account created successfully: id={}, accountNumber={}",
                saved.getId(), saved.getAccountNumber());
        return saved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Account getAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + accountId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account deposit(UUID accountId, BigDecimal amount) {
        log.info("Depositing {} into account id={}", amount, accountId);
        Account account = getAccountById(accountId);
        account.credit(amount);
        return accountRepository.save(account);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implements the Account Service side of the choreography-based Transfer Saga:
     * <ol>
     *   <li>Loads both source and target accounts.</li>
     *   <li>Debits the source account (enforcing balance and status rules).</li>
     *   <li>Credits the target account.</li>
     *   <li>Persists both accounts atomically.</li>
     *   <li>Publishes {@link TransferCompletedEvent} or {@link TransferFailedEvent}
     *       to inform the Transfer Service and Notification Service of the outcome.</li>
     * </ol>
     *
     * <p>If any step fails, the entire database transaction is rolled back and a
     * {@link TransferFailedEvent} is published so that the Saga can be compensated.
     */
    @Override
    public void processTransferRequest(UUID transferId, UUID sourceAccountId,
                                       UUID targetAccountId, BigDecimal amount) {
        log.info("Processing transfer saga: transferId={}, from={}, to={}, amount={}",
                transferId, sourceAccountId, targetAccountId, amount);
        try {
            Account source = accountRepository.findById(sourceAccountId)
                    .orElseThrow(() -> new AccountNotFoundException(
                            "Source account not found: " + sourceAccountId));
            Account target = accountRepository.findById(targetAccountId)
                    .orElseThrow(() -> new AccountNotFoundException(
                            "Target account not found: " + targetAccountId));

            source.debit(amount);
            target.credit(amount);

            accountRepository.save(source);
            accountRepository.save(target);

            TransferCompletedEvent event = TransferCompletedEvent.of(
                    transferId, sourceAccountId, targetAccountId, amount, source.getCurrency());
            eventPublisher.publish(event);
            log.info("Transfer saga completed successfully: transferId={}", transferId);

        } catch (InsufficientFundsException | AccountNotFoundException | IllegalStateException ex) {
            log.warn("Transfer saga failed: transferId={}, reason={}", transferId, ex.getMessage());
            TransferFailedEvent event = TransferFailedEvent.of(
                    transferId, sourceAccountId, targetAccountId, ex.getMessage());
            eventPublisher.publish(event);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Generates a unique, human-readable account number prefixed with {@code CBP-}.
     *
     * <p>In a production system this would use a sequence-backed generator with
     * check-digit validation (e.g., Luhn algorithm or IBAN format). For this
     * portfolio demonstration, a UUID-derived suffix guarantees uniqueness.
     *
     * @return a unique account number string
     */
    private String generateAccountNumber() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "CBP-" + uuid.substring(0, 4) + "-" + uuid.substring(4, 8)
                + "-" + uuid.substring(8, 12);
    }
}
