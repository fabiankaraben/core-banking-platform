-- ============================================================
-- V1: Create notifications table
-- ============================================================
-- Immutable audit log of all customer notification attempts.
-- Managed exclusively by the Notification Service.
-- ============================================================

CREATE TABLE IF NOT EXISTS notifications (
    id          UUID          NOT NULL,
    transfer_id UUID          NOT NULL,
    account_id  UUID          NOT NULL,
    type        VARCHAR(30)   NOT NULL,
    amount      NUMERIC(19,2) NOT NULL,
    currency    VARCHAR(3)    NOT NULL,
    status      VARCHAR(10)   NOT NULL,
    message     VARCHAR(1024) NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT ck_notifications_type CHECK (
        type IN ('TRANSFER_COMPLETED', 'TRANSFER_FAILED')
    ),
    CONSTRAINT ck_notifications_status CHECK (
        status IN ('SENT', 'FAILED')
    )
);

CREATE INDEX IF NOT EXISTS idx_notifications_transfer_id ON notifications (transfer_id);
CREATE INDEX IF NOT EXISTS idx_notifications_account_id  ON notifications (account_id);
CREATE INDEX IF NOT EXISTS idx_notifications_type        ON notifications (type);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at  ON notifications (created_at DESC);

COMMENT ON TABLE notifications IS 'Immutable audit log of all customer notification dispatches.';
COMMENT ON COLUMN notifications.transfer_id IS 'Saga correlation ID linking to the originating transfer.';
COMMENT ON COLUMN notifications.message     IS 'Full rendered notification message sent to the customer.';
