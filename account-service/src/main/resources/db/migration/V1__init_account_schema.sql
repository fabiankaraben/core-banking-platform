CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_customer_id ON accounts(customer_id);
