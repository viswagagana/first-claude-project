package com.payment.ledger.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.ledger.domain.LedgerAccount;
import com.payment.ledger.domain.LedgerBalance;
import com.payment.ledger.domain.LedgerEntry;
import com.payment.ledger.repository.LedgerAccountRepository;
import com.payment.ledger.repository.LedgerBalanceRepository;
import com.payment.ledger.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final ObjectMapper objectMapper;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerBalanceRepository ledgerBalanceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String DLQ_TOPIC = "payment.events.dlq";

    @KafkaListener(topics = {"payment.events", "refund.events"}, groupId = "ledger-service")
    @Transactional
    public void consume(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText(null);
            String eventId = root.path("eventId").asText(null);
            if (eventId == null) eventId = key;
            if (ledgerEntryRepository.existsByEventId(eventId)) {
                log.debug("Duplicate event ignored: {}", eventId);
                return;
            }
            if ("PaymentCaptured".equals(type)) {
                applyPaymentCaptured(root, eventId);
            } else if ("RefundCompleted".equals(type)) {
                applyRefundCompleted(root, eventId);
            }
        } catch (Exception e) {
            log.error("Failed to process payment event, sending to DLQ: key={}", key, e);
            kafkaTemplate.send(DLQ_TOPIC, key, message);
        }
    }

    private void applyPaymentCaptured(JsonNode root, String eventId) throws Exception {
        UUID accountId = UUID.fromString(root.path("accountId").asText());
        String paymentId = root.path("paymentId").asText();
        BigDecimal amount = new BigDecimal(root.path("amount").asText());
        String currency = root.path("currency").asText();
        LedgerAccount ledger = ledgerAccountRepository.findByAccountId(accountId)
            .orElseGet(() -> ledgerAccountRepository.save(LedgerAccount.builder().accountId(accountId).status("ACTIVE").build()));
        LedgerBalance balance = ledgerBalanceRepository.findByLedgerAccountIdAndCurrency(ledger.getId(), currency)
            .orElseGet(() -> ledgerBalanceRepository.save(LedgerBalance.builder()
                .ledgerAccountId(ledger.getId())
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .build()));
        balance.setBalance(balance.getBalance().add(amount));
        ledgerBalanceRepository.save(balance);
        ledgerEntryRepository.save(LedgerEntry.builder()
            .ledgerAccountId(ledger.getId())
            .type("CREDIT")
            .amount(amount)
            .currency(currency)
            .referenceType("payment")
            .referenceId(paymentId)
            .eventId(eventId)
            .build());
    }

    private void applyRefundCompleted(JsonNode root, String eventId) throws Exception {
        UUID accountId = UUID.fromString(root.path("accountId").asText());
        BigDecimal refundAmount = new BigDecimal(root.path("refundAmount").asText());
        String currency = root.path("currency").asText();
        String refundId = root.path("refundId").asText();
        LedgerAccount ledger = ledgerAccountRepository.findByAccountId(accountId).orElse(null);
        if (ledger == null) return;
        LedgerBalance balance = ledgerBalanceRepository.findByLedgerAccountIdAndCurrency(ledger.getId(), currency).orElse(null);
        if (balance == null) return;
        balance.setBalance(balance.getBalance().subtract(refundAmount));
        ledgerBalanceRepository.save(balance);
        ledgerEntryRepository.save(LedgerEntry.builder()
            .ledgerAccountId(ledger.getId())
            .type("DEBIT")
            .amount(refundAmount)
            .currency(currency)
            .referenceType("refund")
            .referenceId(refundId)
            .eventId(eventId)
            .build());
    }
}
