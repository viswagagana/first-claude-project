package com.payment.payment.scheduler;

import com.payment.payment.domain.Payment;
import com.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Reconciliation: compare our payment records with gateway (Stripe/PayPal) and log mismatches.
 * This is a placeholder that logs run status; in production, fetch from Stripe Balance Transactions / PayPal APIs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReconciliationJob {

    private final PaymentRepository paymentRepository;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "${app.reconciliation.cron:0 0 2 * * ?}") // 2 AM daily
    public void runReconciliation() {
        log.info("Reconciliation job started");
        try {
            runStripeReconciliation();
            runPayPalReconciliation();
        } catch (Exception e) {
            log.error("Reconciliation failed", e);
        }
    }

    private void runStripeReconciliation() {
        List<Payment> ours = paymentRepository.findByGateway("stripe");
        int matched = 0;
        int mismatch = 0;
        // TODO: call Stripe API for balance transactions in date range and match by gateway_payment_id
        for (Payment p : ours) {
            matched++;
        }
        logReconciliation("stripe", matched, mismatch, "OK");
    }

    private void runPayPalReconciliation() {
        List<Payment> ours = paymentRepository.findByGateway("paypal");
        int matched = 0;
        int mismatch = 0;
        logReconciliation("paypal", matched, mismatch, "OK");
    }

    private void logReconciliation(String gateway, int matched, int mismatch, String details) {
        try {
            jdbcTemplate.update(
                "INSERT INTO reconciliation_log (gateway, status, matched_count, mismatch_count, details) VALUES (?, ?, ?, ?, ?)",
                gateway, "COMPLETED", matched, mismatch, details
            );
        } catch (Exception e) {
            log.warn("Could not insert reconciliation_log", e);
        }
    }
}
