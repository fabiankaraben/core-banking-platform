-- ============================================================
-- V1: Create transfers table
-- ============================================================
-- Stores transfer intents for the transfer-service bounded
-- context. Managed exclusively by the Transfer Service.
-- ============================================================

CREATE TABLE IF NOT EXISTS transfers (
    id               UUID          NOT NULL,
    source_account_id UUID         NOT NULL,
    target_account_id UUID         NOT NULL,
    amount           NUMERIC(19,2) NOT NULL,
    currency         VARCHAR(3)    NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    description      VARCHAR(255),
    idempotency_key  VARCHAR(255)  NOT NULL,
    failure_reason   VARCHAR(512),
    version          BIGINT        NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_transfers PRIMARY KEY (id),
    CONSTRAINT uq_transfers_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT ck_transfers_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_transfers_accounts_differ CHECK (source_account_id <> target_account_id),
    CONSTRAINT ck_transfers_status CHECK (
        status IN ('PENDING', 'COMPLETED', 'FAILED')
    )
);

CREATE INDEX IF NOT EXISTS idx_transfers_source_account ON transfers (source_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_target_account ON transfers (target_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_status ON transfers (status);
CREATE INDEX IF NOT EXISTS idx_transfers_idempotency_key ON transfers (idempotency_key);

COMMENT ON TABLE transfers IS 'Transfer intent and saga state table for the transfer-service bounded context.';
COMMENT ON COLUMN transfers.id IS 'Saga correlation UUID — used as the Kafka event key.';
COMMENT ON COLUMN transfers.idempotency_key IS 'Client-supplied key; unique constraint prevents duplicate saga initiation.';
COMMENT ON COLUMN transfers.version IS 'JPA optimistic locking version counter.';
