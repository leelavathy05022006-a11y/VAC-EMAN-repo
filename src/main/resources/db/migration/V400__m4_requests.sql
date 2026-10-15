-- Module 4: Emergency Requests, Request Items, Prescriptions, Status History
-- Author: Anusuya

CREATE TABLE IF NOT EXISTS emergency_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_code VARCHAR(20) NOT NULL UNIQUE,
    requester_user_id BIGINT NOT NULL,
    requester_type VARCHAR(10) NOT NULL,
    pharmacy_id BIGINT NOT NULL,
    fulfilment_type VARCHAR(20) NOT NULL,
    priority VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    patient_phone VARCHAR(15) NOT NULL,
    doctor_name VARCHAR(100) NULL,
    doctor_reg_no VARCHAR(40) NULL,
    delivery_address VARCHAR(255) NULL,
    delivery_lat DECIMAL(9,6) NULL,
    delivery_lng DECIMAL(9,6) NULL,
    delivery_partner_user_id BIGINT NULL,
    items_total DECIMAL(10,2) NOT NULL,
    delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(10,2) NOT NULL,
    payment_mode VARCHAR(20) NOT NULL,
    prescription_status VARCHAR(15) NOT NULL DEFAULT 'NONE',
    handover_code CHAR(4) NULL,
    delivery_attempts INT NOT NULL DEFAULT 0,
    stock_committed TINYINT(1) NOT NULL DEFAULT 0,
    notes VARCHAR(500) NULL,
    rejection_reason VARCHAR(255) NULL,
    cancel_reason VARCHAR(255) NULL,
    cancelled_by_role VARCHAR(10) NULL,
    expires_at DATETIME NOT NULL,
    accepted_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_er_requester FOREIGN KEY (requester_user_id) REFERENCES users (id),
    CONSTRAINT fk_er_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacies (id),
    CONSTRAINT fk_er_partner FOREIGN KEY (delivery_partner_user_id) REFERENCES users (id)
);

CREATE INDEX idx_er_requester_status ON emergency_requests (requester_user_id, status);
CREATE INDEX idx_er_pharmacy_status ON emergency_requests (pharmacy_id, status);
CREATE INDEX idx_er_status_expires ON emergency_requests (status, expires_at);

CREATE TABLE IF NOT EXISTS request_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    medicine_name VARCHAR(160) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    requires_prescription TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_ri_request FOREIGN KEY (request_id) REFERENCES emergency_requests (id),
    CONSTRAINT fk_ri_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_ri_inventory FOREIGN KEY (inventory_id) REFERENCES pharmacy_inventory (id)
);

CREATE TABLE IF NOT EXISTS prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NULL,
    uploaded_by_user_id BIGINT NOT NULL,
    file_name VARCHAR(120) NOT NULL,
    original_name VARCHAR(160) NOT NULL,
    content_type VARCHAR(40) NOT NULL,
    file_size INT NOT NULL,
    doctor_name VARCHAR(100) NULL,
    verification_status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    verified_by_user_id BIGINT NULL,
    verified_at DATETIME NULL,
    remarks VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_rx_request FOREIGN KEY (request_id) REFERENCES emergency_requests (id),
    CONSTRAINT fk_rx_uploader FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_rx_verifier FOREIGN KEY (verified_by_user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS request_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    from_status VARCHAR(20) NOT NULL,
    to_status VARCHAR(20) NOT NULL,
    changed_by_user_id BIGINT NULL,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_rsh_request FOREIGN KEY (request_id) REFERENCES emergency_requests (id),
    CONSTRAINT fk_rsh_user FOREIGN KEY (changed_by_user_id) REFERENCES users (id)
);
