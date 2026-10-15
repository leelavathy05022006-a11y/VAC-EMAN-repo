-- Module 4: Delivery Availability and Assignments
-- Author: Anusuya

CREATE TABLE IF NOT EXISTS delivery_availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_user_id BIGINT NOT NULL UNIQUE,
    is_available TINYINT(1) NOT NULL DEFAULT 0,
    current_lat DECIMAL(9,6) NULL,
    current_lng DECIMAL(9,6) NULL,
    last_location_at DATETIME NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_da_partner FOREIGN KEY (partner_user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS delivery_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    partner_user_id BIGINT NOT NULL,
    status VARCHAR(10) NOT NULL,
    attempt_no INT NOT NULL,
    offered_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    responded_at DATETIME NULL,
    reject_reason VARCHAR(255) NULL,
    CONSTRAINT fk_das_request FOREIGN KEY (request_id) REFERENCES emergency_requests (id),
    CONSTRAINT fk_das_partner FOREIGN KEY (partner_user_id) REFERENCES users (id)
);

CREATE INDEX idx_das_partner_status ON delivery_assignments (partner_user_id, status);

-- Seed initial available delivery partners for development (only if matching users exist)
INSERT INTO delivery_availability (partner_user_id, is_available, current_lat, current_lng, last_location_at, updated_at)
SELECT 4, 1, 13.004000, 80.256000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE EXISTS (SELECT 1 FROM users WHERE id = 4)
  AND NOT EXISTS (SELECT 1 FROM delivery_availability WHERE partner_user_id = 4);

INSERT INTO delivery_availability (partner_user_id, is_available, current_lat, current_lng, last_location_at, updated_at)
SELECT 5, 1, 13.010000, 80.250000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE EXISTS (SELECT 1 FROM users WHERE id = 5)
  AND NOT EXISTS (SELECT 1 FROM delivery_availability WHERE partner_user_id = 5);
