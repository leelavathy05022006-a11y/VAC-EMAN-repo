-- V220__m2_chennai_pharmacies_enhancements.sql
-- Module 2: Chennai Pharmacies, Inventory Batches and Update Logs

CREATE TABLE IF NOT EXISTS inventory_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    batch_no VARCHAR(40) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(10,2) NOT NULL,
    expiry_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_inventory FOREIGN KEY (inventory_id) REFERENCES pharmacy_inventory(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS inventory_update_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    pharmacy_id BIGINT NOT NULL,
    update_source VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    log_message VARCHAR(255),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_inventory FOREIGN KEY (inventory_id) REFERENCES pharmacy_inventory(id) ON DELETE CASCADE
);

INSERT IGNORE INTO pharmacies (id, name, address, locality, city, postal_code, contact_phone, latitude, longitude, opening_hours, is_active, is_approved, delivery_available, data_source, last_verified_at) VALUES
(101, 'Apollo Pharmacy - T. Nagar', 'No 14, Sir Theagaraya Rd, Pondy Bazaar, T. Nagar', 'T. Nagar', 'Chennai', '600017', '044-24340055', 13.041800, 80.234100, '24x7', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-01 10:00:00'),
(102, 'MedPlus Pharmacy - Anna Nagar', '2nd Ave, Near Roundtana, Anna Nagar', 'Anna Nagar', 'Chennai', '600040', '044-26211234', 13.085000, 80.210100, '07:00 AM - 11:00 PM', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-02 11:30:00'),
(103, 'Apollo Pharmacy - Velachery', '100 Feet Bypass Rd, Velachery', 'Velachery', 'Chennai', '600042', '044-22435678', 12.978100, 80.220600, '24x7', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-03 09:15:00'),
(104, 'Tulsi Pharmacy - Adyar', 'Lattice Bridge Rd, Adyar', 'Adyar', 'Chennai', '600020', '044-24419876', 13.001200, 80.256500, '08:00 AM - 10:30 PM', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-04 14:00:00'),
(105, 'Netmeds Store - Mylapore', 'Luz Church Rd, Mylapore', 'Mylapore', 'Chennai', '600004', '044-24981122', 13.033900, 80.267600, '08:00 AM - 10:00 PM', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-05 16:45:00'),
(106, 'MedPlus Pharmacy - Tambaram', 'GST Rd, West Tambaram', 'Tambaram', 'Chennai', '600045', '044-22263456', 12.924900, 80.100000, '07:00 AM - 11:00 PM', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-06 12:20:00'),
(107, 'Apollo Pharmacy - Guindy', 'Mount Rd, Near Race Course, Guindy', 'Guindy', 'Chennai', '600032', '044-22348899', 13.006700, 80.202000, '24x7', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-07 08:30:00'),
(108, 'Wellness Forever - Porur', 'Trunk Rd, Porur', 'Porur', 'Chennai', '600116', '044-24769988', 13.038200, 80.156500, '24x7', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-08 15:10:00'),
(109, 'Apollo Pharmacy - Chromepet', 'Radha Nagar Main Rd, Chromepet', 'Chromepet', 'Chennai', '600044', '044-22384455', 12.951600, 80.140200, '07:30 AM - 10:30 PM', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-09 10:40:00'),
(110, 'Pharmeasy Store - Nungambakkam', 'Nungambakkam High Rd, Nungambakkam', 'Nungambakkam', 'Chennai', '600034', '044-28271100', 13.060400, 80.240700, '08:00 AM - 10:00 PM', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-10 09:00:00');
