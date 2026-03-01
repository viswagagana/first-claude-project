package com.payment.ledger.repository;

import com.payment.ledger.domain.LedgerBalance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerBalanceRepository extends org.springframework.data.jpa.repository.JpaRepository<LedgerBalance, UUID> {

    Optional<LedgerBalance> findByLedgerAccountIdAndCurrency(UUID ledgerAccountId, String currency);

    List<LedgerBalance> findByLedgerAccountId(UUID ledgerAccountId);
}
