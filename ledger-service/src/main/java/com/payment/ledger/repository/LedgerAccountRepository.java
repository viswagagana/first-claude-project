package com.payment.ledger.repository;

import com.payment.ledger.domain.LedgerAccount;

import java.util.Optional;
import java.util.UUID;

public interface LedgerAccountRepository extends org.springframework.data.jpa.repository.JpaRepository<LedgerAccount, UUID> {

    Optional<LedgerAccount> findByAccountId(UUID accountId);
}
