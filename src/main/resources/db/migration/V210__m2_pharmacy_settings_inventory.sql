CREATE TABLE pharmacy_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pharmacy_id BIGINT NOT NULL UNIQUE,
    contact_phone VARCHAR(15),
    whatsapp_number VARCHAR(15),
    delivery_enabled TINYINT(1) NOT NULL DEFAULT 0,
    delivery_radius_km DECIMAL(4,1),
    delivery_fee DECIMAL(8,2),
    opens_24x7 TINYINT(1) NOT NULL DEFAULT 0,
    opening_time TIME,
    closing_time TIME,
    accepts_emergency_orders TINYINT(1) NOT NULL DEFAULT 1,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pharmacy_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pharmacy_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    batch_no VARCHAR(40) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(10,2) NOT NULL,
    low_stock_threshold INT NOT NULL DEFAULT 10,
    expiry_date DATE NOT NULL,
    data_source VARCHAR(10) NOT NULL DEFAULT 'SAMPLE',
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    last_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(id),
    CONSTRAINT uk_pharmacy_medicine UNIQUE (pharmacy_id, medicine_id),
    CONSTRAINT chk_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT chk_reserved_non_negative CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_reserved_le_quantity CHECK (reserved_quantity <= quantity)
);

CREATE INDEX idx_inventory_med_pharm ON pharmacy_inventory(medicine_id, pharmacy_id);
CREATE INDEX idx_inventory_pharm_active ON pharmacy_inventory(pharmacy_id, is_active);
CREATE INDEX idx_inventory_expiry ON pharmacy_inventory(expiry_date);

CREATE TABLE inventory_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    txn_type VARCHAR(15) NOT NULL,
    quantity_change INT NOT NULL,
    quantity_after INT NOT NULL,
    reserved_after INT NOT NULL,
    batch_no VARCHAR(40) NOT NULL,
    reference_code VARCHAR(30),
    performed_by_user_id BIGINT,
    remarks VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_inventory FOREIGN KEY (inventory_id) REFERENCES pharmacy_inventory(id)
);
