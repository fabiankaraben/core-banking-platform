package com.cbp.account.application.service;

import com.cbp.account.adapter.in.web.exception.AccountNotFoundException;
import com.cbp.account.domain.event.TransferCompletedEvent;
import com.cbp.account.domain.event.TransferFailedEvent;
import com.cbp.account.domain.model.Account;
import com.cbp.account.domain.model.AccountStatus;
import com.cbp.account.domain.port.out.AccountRepositoryPort;
import com.cbp.account.domain.port.out.EventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link AccountService} application service.
 *
 * <p>Uses Mockito to mock all outbound ports, allowing the application layer
 * to be tested in isolation without a database or Kafka broker.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Application Service")
class AccountServiceTest {

    @Mock
    private AccountRepositoryPort accountRepository;

    @Mock
    private EventPublisherPort eventPublisher;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, eventPublisher);
    }

    private Account activeAccount(UUID id, BigDecimal balance) {
        return new Account(id, "CBP-TEST-0001-0001", "Test Owner", balance,
                "USD", AccountStatus.ACTIVE, 0L, Instant.now(), Instant.now());
    }

    @Nested
    @DisplayName("createAccount")
    class CreateAccountTests {

        @Test
        @DisplayName("should create and save an ACTIVE account")
        void shouldCreateAndSaveActiveAccount() {
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Account result = accountService.createAccount("Alice", "USD");

            assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(result.getOwnerName()).isEqualTo("Alice");
            assertThat(result.getCurrency()).isEqualTo("USD");
            assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(accountRepository).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("getAccountById")
    class GetAccountByIdTests {

        @Test
        @DisplayName("should return account when found")
        void shouldReturnAccountWhenFound() {
            UUID id = UUID.randomUUID();
            Account account = activeAccount(id, BigDecimal.ZERO);
            when(accountRepository.findById(id)).thenReturn(Optional.of(account));

            Account result = accountService.getAccountById(id);

            assertThat(result.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("should throw AccountNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(accountRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccountById(id))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    @Nested
    @DisplayName("deposit")
    class DepositTests {

        @Test
        @DisplayName("should credit account and save")
        void shouldCreditAccountAndSave() {
            UUID id = UUID.randomUUID();
            Account account = activeAccount(id, new BigDecimal("50.00"));
            when(accountRepository.findById(id)).thenReturn(Optional.of(account));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Account result = accountService.deposit(id, new BigDecimal("25.00"));

            assertThat(result.getBalance()).isEqualByComparingTo("75.00");
            verify(accountRepository).save(account);
        }
    }

    @Nested
    @DisplayName("processTransferRequest")
    class ProcessTransferRequestTests {

        @Test
        @DisplayName("should debit source, credit target, and publish TransferCompletedEvent")
        void shouldCompleteTransferSuccessfully() {
            UUID transferId = UUID.randomUUID();
            UUID sourceId = UUID.randomUUID();
            UUID targetId = UUID.randomUUID();
            Account source = activeAccount(sourceId, new BigDecimal("500.00"));
            Account target = activeAccount(targetId, BigDecimal.ZERO);

            when(accountRepository.findById(sourceId)).thenReturn(Optional.of(source));
            when(accountRepository.findById(targetId)).thenReturn(Optional.of(target));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            accountService.processTransferRequest(transferId, sourceId, targetId,
                    new BigDecimal("100.00"));

            assertThat(source.getBalance()).isEqualByComparingTo("400.00");
            assertThat(target.getBalance()).isEqualByComparingTo("100.00");

            verify(eventPublisher).publish(argThat(e -> e instanceof TransferCompletedEvent));
        }

        @Test
        @DisplayName("should publish TransferFailedEvent on insufficient funds")
        void shouldPublishFailedEventOnInsufficientFunds() {
            UUID transferId = UUID.randomUUID();
            UUID sourceId = UUID.randomUUID();
            UUID targetId = UUID.randomUUID();
            Account source = activeAccount(sourceId, new BigDecimal("10.00"));
            Account target = activeAccount(targetId, BigDecimal.ZERO);

            when(accountRepository.findById(sourceId)).thenReturn(Optional.of(source));
            when(accountRepository.findById(targetId)).thenReturn(Optional.of(target));

            accountService.processTransferRequest(transferId, sourceId, targetId,
                    new BigDecimal("500.00"));

            verify(eventPublisher).publish(argThat(e -> e instanceof TransferFailedEvent));
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("should publish TransferFailedEvent when source account not found")
        void shouldPublishFailedEventWhenSourceNotFound() {
            UUID transferId = UUID.randomUUID();
            UUID sourceId = UUID.randomUUID();
            UUID targetId = UUID.randomUUID();

            when(accountRepository.findById(sourceId)).thenReturn(Optional.empty());

            accountService.processTransferRequest(transferId, sourceId, targetId,
                    new BigDecimal("100.00"));

            verify(eventPublisher).publish(argThat(e -> e instanceof TransferFailedEvent));
        }
    }

    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccountsTests {

        @Test
        @DisplayName("should return all accounts from repository")
        void shouldReturnAllAccounts() {
            List<Account> accounts = List.of(
                    activeAccount(UUID.randomUUID(), BigDecimal.ZERO),
                    activeAccount(UUID.randomUUID(), new BigDecimal("100.00"))
            );
            when(accountRepository.findAll()).thenReturn(accounts);

            List<Account> result = accountService.getAllAccounts();

            assertThat(result).hasSize(2);
        }
    }
}
