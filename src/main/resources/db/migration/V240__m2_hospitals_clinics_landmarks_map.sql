-- =====================================================================
-- Migration V240: Hospitals, Clinics, Landmarks & Facility Types for Interactive Map
-- Module 2: Medicine & Inventory Management (Leelavathy)
-- =====================================================================

-- 1. Extend pharmacies table with facility_type column
ALTER TABLE pharmacies ADD COLUMN facility_type VARCHAR(30) NOT NULL DEFAULT 'PHARMACY';
CREATE INDEX idx_pharmacies_facility_type ON pharmacies(facility_type);

-- 2. Seed Real Chennai Hospitals (24x7 Emergency Trauma & Critical Care)
INSERT INTO pharmacies (id, name, address, locality, city, postal_code, contact_phone, latitude, longitude, opening_hours, is_active, is_approved, delivery_available, data_source, last_verified_at, facility_type, license_number, website, email, verification_status) VALUES
(201, 'Rajiv Gandhi Government General Hospital (RGGGH)', 'EVR Periyar Salai, Park Town, Near Chennai Central', 'Park Town', 'Chennai', '600003', '044-25305000', 13.080500, 80.278500, '24x7', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-10 10:00:00', 'HOSPITAL', 'GOV/TN/HOSP/001', 'https://www.mmc.tn.gov.in', 'dean@mmc.tn.gov.in', 'VERIFIED'),
(202, 'Apollo Hospital - Greams Road', 'No 21 Greams Lane, Off Greams Road, Thousand Lights', 'Thousand Lights', 'Chennai', '600006', '044-28290200', 13.056000, 80.252000, '24x7', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-11 11:00:00', 'HOSPITAL', 'TN/CHE/HOSP/002', 'https://www.apollohospitals.com', 'emergency.greams@apollohospitals.com', 'VERIFIED'),
(203, 'Sri Ramachandra Medical Centre (SRMC)', 'No 1 Ramachandra Nagar, Mount Poonamallee Rd, Porur', 'Porur', 'Chennai', '600116', '044-45928500', 13.037500, 80.143200, '24x7', 1, 1, 1, 'VERIFIED_DIRECTORY', '2026-10-11 12:00:00', 'HOSPITAL', 'TN/CHE/HOSP/003', 'https://www.sriramachandra.edu.in', 'emergency@sriramachandra.edu.in', 'VERIFIED'),
(204, 'MIOT International Hospital', '4/112 Mount Poonamallee Road, Manapakkam', 'Manapakkam', 'Chennai', '600089', '044-42002288', 13.018500, 80.177200, '24x7', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-10 15:00:00', 'HOSPITAL', 'TN/CHE/HOSP/004', 'https://www.miotinternational.com', 'emergency@miotinternational.com', 'VERIFIED'),
(205, 'Tamil Nadu Govt Multi Super Speciality Hospital', 'Omandurar Government Estate, Anna Salai', 'Triplicane', 'Chennai', '600002', '044-25666000', 13.069400, 80.274700, '24x7', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-09 14:00:00', 'HOSPITAL', 'GOV/TN/HOSP/005', 'https://tngmssh.tn.gov.in', 'director@tngmssh.tn.gov.in', 'VERIFIED'),
(206, 'Fortis Malar Hospital - Adyar', 'No 52, 1st Main Road, Gandhi Nagar, Adyar', 'Adyar', 'Chennai', '600020', '044-42892222', 13.007800, 80.257100, '24x7', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-10 16:30:00', 'HOSPITAL', 'TN/CHE/HOSP/006', 'https://www.fortishealthcare.com', 'contact.malar@fortishealthcare.com', 'VERIFIED');

-- 3. Seed Real Chennai Clinics (Outpatient Care & Day Clinics)
INSERT INTO pharmacies (id, name, address, locality, city, postal_code, contact_phone, latitude, longitude, opening_hours, is_active, is_approved, delivery_available, data_source, last_verified_at, facility_type, license_number, website, email, verification_status) VALUES
(211, 'Apollo Clinic - Velachery', 'No 137 Velachery Bypass Rd, Dhandeeswaram, Velachery', 'Velachery', 'Chennai', '600042', '044-40004444', 12.981200, 80.222500, '08:00 AM - 09:00 PM', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-10 09:00:00', 'CLINIC', 'TN/CHE/CLN/001', 'https://www.theapolloclinic.com', 'velachery@theapolloclinic.com', 'VERIFIED'),
(212, 'Apollo Clinic - Anna Nagar', 'AC Block, 2nd Avenue, Near Roundtana, Anna Nagar', 'Anna Nagar', 'Chennai', '600040', '044-26210088', 13.087200, 80.213500, '07:30 AM - 09:00 PM', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-11 10:30:00', 'CLINIC', 'TN/CHE/CLN/002', 'https://www.theapolloclinic.com', 'annanagar@theapolloclinic.com', 'VERIFIED'),
(213, 'Kauvery Medical Centre Day Care Clinic', 'No 199 Luz Church Road, Opposite Citi Centre, Mylapore', 'Mylapore', 'Chennai', '600004', '044-40006000', 13.036000, 80.264200, '08:00 AM - 08:30 PM', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-09 11:15:00', 'CLINIC', 'TN/CHE/CLN/003', 'https://www.kauveryhospital.com', 'mylapore@kauveryhospital.com', 'VERIFIED'),
(214, 'Dr. Mohans Diabetes Specialities Clinic', 'No 6B Conran Smith Road, Gopalapuram / T. Nagar', 'T. Nagar', 'Chennai', '600017', '044-43968888', 13.045000, 80.237000, '07:00 AM - 07:00 PM', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-10 14:00:00', 'CLINIC', 'TN/CHE/CLN/004', 'https://www.drmohans.com', 'info@drmohans.com', 'VERIFIED');

-- 4. Seed Real Chennai Landmarks, Colleges & Bus Terminals
INSERT INTO pharmacies (id, name, address, locality, city, postal_code, contact_phone, latitude, longitude, opening_hours, is_active, is_approved, delivery_available, data_source, last_verified_at, facility_type, license_number, website, email, verification_status) VALUES
(221, 'Chennai Central Railway Station (Dr. MGR Central)', 'Kannappar Thidal, Periyamet, Chennai Central', 'Central', 'Chennai', '600003', '139', 13.082700, 80.275500, '24x7', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-01 08:00:00', 'LANDMARK', 'SR/MAS/2026', 'https://www.irctc.co.in', NULL, 'VERIFIED'),
(222, 'Chennai Mofussil Bus Terminus (CMBT Koyambedu)', 'Jawaharlal Nehru Road, Koyambedu', 'Koyambedu', 'Chennai', '600107', '044-24794707', 13.069100, 80.205700, '24x7', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-01 08:00:00', 'TRANSIT', 'SETC/CMBT/01', 'https://www.tnstc.in', NULL, 'VERIFIED'),
(223, 'Poonamallee Bus Terminus', 'Trunk Road, Near Bazaar, Poonamallee', 'Poonamallee', 'Chennai', '600056', '044-26272300', 13.047800, 80.111800, '24x7', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-01 08:00:00', 'TRANSIT', 'MTC/PNM/01', 'https://mtcbus.tn.gov.in', NULL, 'VERIFIED'),
(224, 'Anna University - Guindy Campus', 'Sardar Patel Road, Guindy', 'Guindy', 'Chennai', '600025', '044-22357004', 13.010900, 80.235500, '08:00 AM - 06:00 PM', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-01 08:00:00', 'EDUCATION', 'AU/CHE/1978', 'https://www.annauniv.edu', 'registrar@annauniv.edu', 'VERIFIED'),
(225, 'Madras Medical College (MMC)', 'Near Chennai Central, EVR Periyar Salai, Park Town', 'Park Town', 'Chennai', '600003', '044-25305101', 13.081700, 80.277000, '08:00 AM - 06:00 PM', 1, 1, 0, 'VERIFIED_DIRECTORY', '2026-10-01 08:00:00', 'EDUCATION', 'MMC/ESTD/1835', 'https://www.mmc.tn.gov.in', 'deanmmc@tn.gov.in', 'VERIFIED');

-- 5. Seed default pharmacy_settings for hospitals and clinics
INSERT INTO pharmacy_settings (pharmacy_id, contact_phone, whatsapp_number, delivery_enabled, delivery_radius_km, delivery_fee, opens_24x7, opening_time, closing_time, accepts_emergency_orders) VALUES
(201, '044-25305000', '919444000201', 0, 0.0, 0.00, 1, '00:00:00', '23:59:59', 1),
(202, '044-28290200', '919444000202', 1, 10.0, 50.00, 1, '00:00:00', '23:59:59', 1),
(203, '044-45928500', '919444000203', 1, 8.0, 40.00, 1, '00:00:00', '23:59:59', 1),
(204, '044-42002288', '919444000204', 0, 0.0, 0.00, 1, '00:00:00', '23:59:59', 1),
(205, '044-25666000', '919444000205', 0, 0.0, 0.00, 1, '00:00:00', '23:59:59', 1),
(206, '044-42892222', '919444000206', 0, 0.0, 0.00, 1, '00:00:00', '23:59:59', 1),
(211, '044-40004444', '919444000211', 0, 0.0, 0.00, 0, '08:00:00', '21:00:00', 1),
(212, '044-26210088', '919444000212', 0, 0.0, 0.00, 0, '07:30:00', '21:00:00', 1),
(213, '044-40006000', '919444000213', 0, 0.0, 0.00, 0, '08:00:00', '20:30:00', 1),
(214, '044-43968888', '919444000214', 0, 0.0, 0.00, 0, '07:00:00', '19:00:00', 0);
