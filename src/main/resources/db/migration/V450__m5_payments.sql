-- Module 5: Payment Management (Gateway-Independent)
-- Flyway Migration: V450__m5_payments.sql

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_reference VARCHAR(32) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    pharmacy_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(5) NOT NULL DEFAULT 'INR',
    payment_method VARCHAR(20) NOT NULL, -- 'UPI', 'UPI_QR', 'DEBIT_CARD', 'CREDIT_CARD', 'COD'
    payment_status VARCHAR(20) NOT NULL DEFAULT 'CREATED', -- 'CREATED', 'PENDING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED'
    gateway_provider VARCHAR(30) NOT NULL DEFAULT 'DEMO',
    gateway_transaction_id VARCHAR(100) NULL,
    idempotency_key VARCHAR(64) NULL UNIQUE,
    failure_reason VARCHAR(255) NULL,
    cash_collected_by_user_id BIGINT NULL,
    cash_collected_at DATETIME NULL,
    paid_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES emergency_requests(id),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payment_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(payment_status);
CREATE INDEX idx_payments_ref ON payments(payment_reference);
