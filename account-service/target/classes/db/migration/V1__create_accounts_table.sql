-- ============================================================
-- V1: Create accounts table
-- ============================================================
-- Stores bank account entities for the account-service bounded
-- context. Managed exclusively by the Account Service —
-- no other service may write to this database.
-- ============================================================

CREATE TABLE IF NOT EXISTS accounts (
    id             UUID         NOT NULL,
    account_number VARCHAR(24)  NOT NULL,
    owner_name     VARCHAR(100) NOT NULL,
    balance        NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    currency       CHAR(3)      NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    version        BIGINT       NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_account_number UNIQUE (account_number),
    CONSTRAINT ck_accounts_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT ck_accounts_status CHECK (
        status IN ('PENDING_ACTIVATION', 'ACTIVE', 'FROZEN', 'CLOSED')
    )
);

CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts (account_number);
CREATE INDEX IF NOT EXISTS idx_accounts_status ON accounts (status);
CREATE INDEX IF NOT EXISTS idx_accounts_owner_name ON accounts (owner_name);

COMMENT ON TABLE accounts IS 'Core account entity table for the account-service bounded context.';
COMMENT ON COLUMN accounts.id IS 'Surrogate primary key (UUID).';
COMMENT ON COLUMN accounts.account_number IS 'Human-readable unique account number (CBP-XXXX-YYYY-ZZZZ).';
COMMENT ON COLUMN accounts.balance IS 'Current available balance with NUMERIC precision to avoid float rounding.';
COMMENT ON COLUMN accounts.version IS 'JPA optimistic locking version counter.';
