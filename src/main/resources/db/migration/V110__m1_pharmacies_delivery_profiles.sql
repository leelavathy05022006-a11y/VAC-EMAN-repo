-- V110__m1_pharmacies_delivery_profiles.sql
-- Module 1: Pharmacy and Delivery Partner Profiles
-- Enhanced to support both User-registered Pharmacies and Verified Directory Pharmacies

CREATE TABLE IF NOT EXISTS pharmacies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    license_no VARCHAR(50) NULL UNIQUE,
    
    gst_no VARCHAR(20) NULL,
    address VARCHAR(255) NOT NULL,
    locality VARCHAR(80) NULL,
    city VARCHAR(60) NOT NULL DEFAULT 'Chennai',
    pincode VARCHAR(10) NULL,
    postal_code VARCHAR(10) NULL,
    contact_phone VARCHAR(20) NULL,
    phone_number VARCHAR(20) NULL,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    opening_hours VARCHAR(100) NULL,
    approval_status VARCHAR(15) NOT NULL DEFAULT 'APPROVED',
    is_approved TINYINT(1) NOT NULL DEFAULT 1,
    rejection_reason VARCHAR(255) NULL,
    approved_by_user_id BIGINT NULL,
    approved_at DATETIME NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    is_demo TINYINT(1) NOT NULL DEFAULT 0,
    delivery_available TINYINT(1) NOT NULL DEFAULT 0,
    data_source VARCHAR(25) NOT NULL DEFAULT 'LIVE',
    
    last_verified_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pharmacy_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_pharmacy_approved_by FOREIGN KEY (approved_by_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS delivery_partner_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    vehicle_type VARCHAR(20) NULL,
    vehicle_number VARCHAR(20) NULL,
    driving_license_no VARCHAR(30) NULL,
    service_city VARCHAR(60) NULL DEFAULT 'Chennai',
    is_verified TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_delivery_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_pharmacies_approval_status ON pharmacies(approval_status);
CREATE INDEX idx_pharmacies_location ON pharmacies(latitude, longitude);
CREATE INDEX idx_pharmacies_city_locality ON pharmacies(city, locality);
