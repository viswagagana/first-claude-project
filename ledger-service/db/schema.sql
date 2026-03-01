-- Ledger Service schema

CREATE TABLE IF NOT EXISTS ledger_accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id       UUID NOT NULL UNIQUE,
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS ledger_balances (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ledger_account_id UUID NOT NULL REFERENCES ledger_accounts(id) ON DELETE CASCADE,
    currency        VARCHAR(3) NOT NULL,
    balance         DECIMAL(19,4) NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp,
    UNIQUE(ledger_account_id, currency)
);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id              BIGSERIAL PRIMARY KEY,
    ledger_account_id UUID NOT NULL REFERENCES ledger_accounts(id) ON DELETE CASCADE,
    type            VARCHAR(50) NOT NULL,
    amount          DECIMAL(19,4) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    reference_type  VARCHAR(100),
    reference_id    VARCHAR(255),
    event_id        VARCHAR(255) UNIQUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp
);

CREATE INDEX idx_ledger_accounts_account_id ON ledger_accounts(account_id);
CREATE INDEX idx_ledger_balances_ledger_account_id ON ledger_balances(ledger_account_id);
CREATE INDEX idx_ledger_entries_ledger_account_id ON ledger_entries(ledger_account_id);
CREATE INDEX idx_ledger_entries_event_id ON ledger_entries(event_id);
