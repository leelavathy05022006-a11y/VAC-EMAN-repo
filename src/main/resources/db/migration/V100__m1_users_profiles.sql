-- V100__m1_users_profiles.sql
-- Module 1: User & Authentication Management
-- Author: Divya

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS patient_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    date_of_birth DATE NULL,
    gender VARCHAR(10) NULL,
    blood_group VARCHAR(5) NULL,
    address VARCHAR(255) NULL,
    city VARCHAR(60) NULL,
    pincode CHAR(6) NULL,
    latitude DECIMAL(9,6) NULL,
    longitude DECIMAL(9,6) NULL,
    emergency_contact_name VARCHAR(100) NULL,
    emergency_contact_phone VARCHAR(15) NULL,
    CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS hospital_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    hospital_name VARCHAR(150) NOT NULL,
    registration_no VARCHAR(50) NOT NULL UNIQUE,
    hospital_type VARCHAR(30) NULL,
    emergency_phone VARCHAR(15) NULL,
    address VARCHAR(255) NULL,
    city VARCHAR(60) NULL,
    pincode CHAR(6) NULL,
    latitude DECIMAL(9,6) NULL,
    longitude DECIMAL(9,6) NULL,
    CONSTRAINT fk_hospital_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_active ON users(role, is_active);
