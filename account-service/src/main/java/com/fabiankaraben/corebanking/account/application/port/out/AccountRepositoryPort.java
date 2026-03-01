package com.fabiankaraben.corebanking.account.application.port.out;

import com.fabiankaraben.corebanking.account.domain.Account;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Account save(Account account);
    Optional<Account> findById(UUID id);
}
