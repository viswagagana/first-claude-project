-- Payment Service schema

CREATE TABLE IF NOT EXISTS idempotency_keys (
    idempotency_key   VARCHAR(255) PRIMARY KEY,
    response_status   INT NOT NULL,
    response_body     TEXT,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id          UUID NOT NULL,
    idempotency_key     VARCHAR(255) UNIQUE,
    gateway             VARCHAR(50) NOT NULL,
    gateway_payment_id  VARCHAR(255),
    gateway_capture_id  VARCHAR(255),
    payment_method_id   UUID,
    amount              DECIMAL(19,4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    status              VARCHAR(50) NOT NULL,
    description         TEXT,
    metadata            JSONB,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS refunds (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id          UUID NOT NULL,
    idempotency_key     VARCHAR(255) UNIQUE,
    amount              DECIMAL(19,4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    gateway_refund_id   VARCHAR(255),
    status              VARCHAR(50) NOT NULL,
    reason              TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(255) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp,
    published_at    TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS audit_log (
    id              BIGSERIAL PRIMARY KEY,
    action          VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(100),
    resource_id     VARCHAR(255),
    account_id      UUID,
    details         TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS reconciliation_log (
    id              BIGSERIAL PRIMARY KEY,
    gateway         VARCHAR(50) NOT NULL,
    run_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp,
    status          VARCHAR(50) NOT NULL,
    matched_count   INT DEFAULT 0,
    mismatch_count  INT DEFAULT 0,
    details         TEXT
);

CREATE INDEX idx_payments_account_id ON payments(account_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_outbox_events_published_at ON outbox_events(published_at) WHERE published_at IS NULL;
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX idx_reconciliation_log_run_at ON reconciliation_log(run_at);
