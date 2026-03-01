# ADR 001: Use Kafka for payment and refund events

## Status

Accepted.

## Context

We need eventual consistency between Payment Service, Ledger Service, and Notification Service. Payment and refund outcomes must be reflected in the ledger and trigger notifications without blocking the payment API.

## Decision

- Use **Apache Kafka** as the message broker for payment and refund domain events.
- Payment Service writes events to a **transactional outbox** in the same database as payments, then a relay job publishes from the outbox to Kafka. This avoids “DB committed but message lost” and keeps at-least-once delivery.
- Topics: `payment.events` (PaymentCaptured, PaymentFailed) and `refund.events` (RefundCompleted, RefundFailed).
- Ledger and Notification services consume from these topics with **idempotent processing** (deduplication by event id) and send failed messages to a **Dead Letter Queue** (DLQ) topic for inspection and replay.

## Consequences

- Eventual consistency: ledger and notifications may lag by seconds.
- We gain resilience to consumer downtime and the ability to replay from offsets or DLQ.
- Operational cost: run and monitor Kafka (and Zookeeper if not using KRaft).
