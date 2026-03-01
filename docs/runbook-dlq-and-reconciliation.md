# Runbook: DLQ and reconciliation

## Dead Letter Queue (DLQ)

Failed consumption of payment/refund events can be sent to a DLQ topic so that no event is lost.

### Topics

- **Ledger:** failed events from `payment.events` / `refund.events` are sent to `payment.events.dlq`.
- **Notification:** failed events are sent to `notification.events.dlq`.

### What to do

1. **Monitor** DLQ depth (e.g. via Kafka tooling or metrics). Alert if depth grows.
2. **Inspect** messages: check logs and payload to see why processing failed (e.g. bad payload, DB constraint, bug).
3. **Fix** the cause (data fix, code fix, or config).
4. **Replay:** republish messages from DLQ back to the main topic (or a replay topic) and ensure idempotency so duplicates do not double-apply. Optionally delete from DLQ after successful processing.

### Example (Kafka CLI)

List DLQ messages (example):

```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic payment.events.dlq --from-beginning
```

Replay by producing the same key/value to `payment.events` (then clear or skip the DLQ offset as needed).

## Reconciliation

The Payment Service runs a **reconciliation job** (scheduled, e.g. daily at 2 AM) that compares our payment/refund records with the gateway (Stripe/PayPal) and logs results to `reconciliation_log` table.

### What to do

1. **Query** `reconciliation_log` for status and `mismatch_count`:
   - `SELECT * FROM reconciliation_log ORDER BY run_at DESC LIMIT 20;`
2. If **mismatch_count > 0**, investigate: fetch gateway data for the date range and compare with our `payments` and `refunds` tables. Fix data or process missing webhooks/refunds as needed.
3. **Stripe:** use Stripe Dashboard or Balance Transactions API. **PayPal:** use PayPal reports or API.
4. Fix any bugs in the reconciliation job or in payment/refund processing that cause systematic mismatches.
