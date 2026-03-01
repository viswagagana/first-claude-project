package com.payment.ledger.repository;

import com.payment.ledger.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    boolean existsByEventId(String eventId);

    List<LedgerEntry> findByLedgerAccountIdOrderByCreatedAtDesc(UUID ledgerAccountId, org.springframework.data.domain.Pageable pageable);
}
