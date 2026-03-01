CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    transfer_id UUID NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_notifications_transfer ON notifications(transfer_id);
