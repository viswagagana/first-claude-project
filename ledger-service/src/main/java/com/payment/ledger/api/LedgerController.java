package com.payment.ledger.api;

import com.payment.ledger.domain.LedgerBalance;
import com.payment.ledger.domain.LedgerEntry;
import com.payment.ledger.repository.LedgerAccountRepository;
import com.payment.ledger.repository.LedgerBalanceRepository;
import com.payment.ledger.repository.LedgerEntryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Ledger", description = "Balances and ledger entries")
public class LedgerController {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerBalanceRepository ledgerBalanceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @GetMapping("/ledger/accounts/{accountId}/balances")
    @Operation(summary = "Get balances for account")
    public ResponseEntity<List<BalanceDto>> getBalances(@PathVariable UUID accountId) {
        return ledgerAccountRepository.findByAccountId(accountId)
            .map(ledger -> ledgerBalanceRepository.findByLedgerAccountId(ledger.getId()).stream()
                .map(BalanceDto::from)
                .toList())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ledger/accounts/{accountId}/entries")
    @Operation(summary = "Get ledger entries for account")
    public ResponseEntity<List<EntryDto>> getEntries(@PathVariable UUID accountId, @RequestParam(defaultValue = "50") int size) {
        return ledgerAccountRepository.findByAccountId(accountId)
            .map(ledger -> ledgerEntryRepository.findByLedgerAccountIdOrderByCreatedAtDesc(ledger.getId(), PageRequest.of(0, size)).stream()
                .map(EntryDto::from)
                .toList())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    public record BalanceDto(String currency, java.math.BigDecimal balance) {
        static BalanceDto from(LedgerBalance b) {
            return new BalanceDto(b.getCurrency(), b.getBalance());
        }
    }

    public record EntryDto(Long id, String type, java.math.BigDecimal amount, String currency, String referenceType, String referenceId, String createdAt) {
        static EntryDto from(LedgerEntry e) {
            return new EntryDto(e.getId(), e.getType(), e.getAmount(), e.getCurrency(), e.getReferenceType(), e.getReferenceId(), e.getCreatedAt().toString());
        }
    }
}
