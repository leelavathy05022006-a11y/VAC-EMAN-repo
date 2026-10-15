-- Module 3: Medicine Search & Availability (Deepika)
-- Flyway Migration: V300__m3_search_contact_logs.sql

CREATE TABLE IF NOT EXISTS search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    medicine_id BIGINT NULL,
    query_text VARCHAR(120),
    quantity INT,
    user_lat DECIMAL(6,3),
    user_lng DECIMAL(6,3),
    radius_km DECIMAL(4,1),
    sort_by VARCHAR(15),
    results_count INT,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_search_history_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_search_history_user_created ON search_history (user_id, created_at);

CREATE TABLE IF NOT EXISTS contact_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pharmacy_id BIGINT NOT NULL,
    medicine_id BIGINT NULL,
    contact_type VARCHAR(10) NOT NULL, -- 'CALL', 'WHATSAPP', 'NAVIGATION'
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_contact_logs_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_contact_logs_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacies (id),
    CONSTRAINT fk_contact_logs_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_contact_logs_pharmacy_created ON contact_logs (pharmacy_id, created_at);
