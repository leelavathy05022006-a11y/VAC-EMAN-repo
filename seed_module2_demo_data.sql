-- ====================================================================================================
-- EMERGENCY MEDICINE AVAILABILITY NETWORK (EMAN)
-- MODULE 2: MEDICINE & INVENTORY MANAGEMENT - DUMMY DATABASE SEED SCRIPT
-- ====================================================================================================
-- Purpose: Populates sample demonstration data for Module 2:
--   1. 10 Verified Chennai Pharmacies across Anna Nagar, Poonamallee, Porur, Tambaram, Velachery,
--      T. Nagar, Ambattur, Iyyappanthangal, Adyar, Guindy.
--   2. Pharmacy Settings (Delivery availability, 24x7 hours, delivery fee, radius).
--   3. 9 Core Medicine Categories.
--   4. 18 Essential Medicines (Paracetamol, Cetirizine, ORS, Vitamin C, Omeprazole, Pantoprazole, etc.).
--   5. Realistic Inventory Records with stock quantities, batches, INR prices, and valid expiry dates.
--
-- Safety & Idempotency: Uses INSERT IGNORE / ON DUPLICATE KEY UPDATE.
-- Can be executed repeatedly without causing primary or unique key constraint violations.
-- ====================================================================================================

USE emergency_med_db;

-- ----------------------------------------------------------------------------------------------------
-- 1. SEED / UPDATE CHENNAI SAMPLE PHARMACIES (10 Localities)
-- ----------------------------------------------------------------------------------------------------
INSERT INTO pharmacies (
    id, name, address, locality, city, postal_code, contact_phone, 
    latitude, longitude, opening_hours, is_active, is_approved, 
    approval_status, delivery_available, data_source, last_verified_at,
    license_number, website, email, verification_status
) VALUES
(101, 'Apollo Pharmacy - T. Nagar', 'No 14, Sir Theagaraya Rd, Pondy Bazaar, T. Nagar', 'T. Nagar', 'Chennai', '600017', '044-24340055', 13.041800, 80.234100, '24x7', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-01 10:00:00', 'TN/CHE/20B/10101', 'https://www.apollopharmacy.in', 'tnagar@apollopharmacy.in', 'VERIFIED'),
(102, 'MedPlus Pharmacy - Anna Nagar', '2nd Ave, Near Roundtana, Anna Nagar', 'Anna Nagar', 'Chennai', '600040', '044-26211234', 13.085000, 80.210100, '07:00 AM - 11:00 PM', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-02 11:30:00', 'TN/CHE/21B/10102', 'https://www.medplusmart.com', 'annanagar@medplusmart.com', 'VERIFIED'),
(103, 'Apollo Pharmacy - Velachery', '100 Feet Bypass Rd, Velachery', 'Velachery', 'Chennai', '600042', '044-22435678', 12.978100, 80.220600, '24x7', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-03 09:15:00', 'TN/CHE/20B/10103', 'https://www.apollopharmacy.in', 'velachery@apollopharmacy.in', 'VERIFIED'),
(104, 'Tulsi Pharmacy - Adyar', 'Lattice Bridge Rd, Adyar', 'Adyar', 'Chennai', '600020', '044-24419876', 13.001200, 80.256500, '08:00 AM - 10:30 PM', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-04 14:00:00', 'TN/CHE/20B/10104', NULL, 'tulsipharmacy.adyar@gmail.com', 'VERIFIED'),
(106, 'MedPlus Pharmacy - Tambaram', 'GST Rd, West Tambaram', 'Tambaram', 'Chennai', '600045', '044-22263456', 12.924900, 80.100000, '07:00 AM - 11:00 PM', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-06 12:20:00', 'TN/CHE/21B/10106', 'https://www.medplusmart.com', 'tambaram@medplusmart.com', 'VERIFIED'),
(107, 'Apollo Pharmacy - Guindy', 'Mount Rd, Near Race Course, Guindy', 'Guindy', 'Chennai', '600032', '044-22348899', 13.006700, 80.202000, '24x7', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-07 08:30:00', 'TN/CHE/20B/10107', 'https://www.apollopharmacy.in', 'guindy@apollopharmacy.in', 'VERIFIED'),
(108, 'Wellness Forever - Porur', 'Trunk Rd, Porur', 'Porur', 'Chennai', '600116', '044-24769988', 13.038200, 80.156500, '24x7', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-08 15:10:00', 'TN/CHE/21B/11108', 'https://www.wellnessforever.com', 'porur@wellnessforever.in', 'VERIFIED'),
(111, 'Apollo Pharmacy - Poonamallee Trunk Road', 'No 28, Trunk Road, Near Bus Terminus, Poonamallee', 'Poonamallee', 'Chennai', '600056', '044-26272211', 13.048900, 80.113200, '24x7', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-10 10:00:00', 'TN/CHE/20B/14111', 'https://www.apollopharmacy.in', 'poonamallee@apollopharmacy.in', 'VERIFIED'),
(115, 'MedPlus Pharmacy - Iyyappanthangal', 'Mount Poonamallee High Rd, Near Bus Depot, Iyyappanthangal', 'Iyyappanthangal', 'Chennai', '600056', '044-24765500', 13.039000, 80.141500, '24x7', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-10 14:00:00', 'TN/CHE/21B/18115', 'https://www.medplusmart.com', 'iyyappanthangal@medplusmart.com', 'VERIFIED'),
(122, 'MedPlus Pharmacy - Ambattur OT', 'MTH Road, Near Old Bus Stand, Ambattur', 'Ambattur', 'Chennai', '600053', '044-26581122', 13.114300, 80.154800, '07:00 AM - 11:00 PM', 1, 1, 'APPROVED', 1, 'VERIFIED_DIRECTORY', '2026-10-09 11:00:00', 'TN/CHE/21B/25122', 'https://www.medplusmart.com', 'ambattur@medplusmart.com', 'VERIFIED')
ON DUPLICATE KEY UPDATE 
    is_active = 1,
    is_approved = 1,
    approval_status = 'APPROVED',
    delivery_available = 1,
    contact_phone = VALUES(contact_phone),
    address = VALUES(address),
    opening_hours = VALUES(opening_hours);

-- ----------------------------------------------------------------------------------------------------
-- 2. SEED PHARMACY SETTINGS (Delivery Radius, Fee, Hours, Emergency Ordering)
-- ----------------------------------------------------------------------------------------------------
INSERT INTO pharmacy_settings (
    pharmacy_id, contact_phone, whatsapp_number, delivery_enabled,
    delivery_radius_km, delivery_fee, opens_24x7, opening_time, closing_time,
    accepts_emergency_orders
) VALUES
(101, '044-24340055', '919876543210', 1, 10.0, 30.00, 1, '00:00:00', '23:59:59', 1),
(102, '044-26211234', '919876543211', 1, 8.0, 35.00, 0, '07:00:00', '23:00:00', 1),
(103, '044-22435678', '919876543212', 1, 10.0, 30.00, 1, '00:00:00', '23:59:59', 1),
(104, '044-24419876', '919876543213', 1, 6.0, 40.00, 0, '08:00:00', '22:30:00', 1),
(106, '044-22263456', '919876543214', 1, 10.0, 35.00, 0, '07:00:00', '23:00:00', 1),
(107, '044-22348899', '919876543215', 1, 12.0, 30.00, 1, '00:00:00', '23:59:59', 1),
(108, '044-24769988', '919876543216', 1, 10.0, 30.00, 1, '00:00:00', '23:59:59', 1),
(111, '044-26272211', '919876543217', 1, 10.0, 25.00, 1, '00:00:00', '23:59:59', 1),
(115, '044-24765500', '919876543218', 1, 8.0, 30.00, 1, '00:00:00', '23:59:59', 1),
(122, '044-26581122', '919876543219', 1, 10.0, 30.00, 0, '07:00:00', '23:00:00', 1)
ON DUPLICATE KEY UPDATE
    delivery_enabled = 1,
    delivery_radius_km = VALUES(delivery_radius_km),
    delivery_fee = VALUES(delivery_fee),
    opens_24x7 = VALUES(opens_24x7),
    accepts_emergency_orders = 1;

-- ----------------------------------------------------------------------------------------------------
-- 3. SEED MEDICINE CATEGORIES
-- ----------------------------------------------------------------------------------------------------
INSERT IGNORE INTO medicine_categories (id, name, description, is_active) VALUES
(1, 'Analgesics & Antipyretics', 'Pain relief, fever reducers and anti-inflammatory medications', 1),
(2, 'Antibiotics', 'Broad-spectrum antibacterial and antimicrobial therapeutics', 1),
(3, 'Antidiabetics', 'Blood glucose regulators, oral hypoglycemics, and insulin', 1),
(4, 'Cardiovascular', 'Antihypertensives, cholesterol regulators, and cardiac support', 1),
(5, 'Antihistamines', 'Allergy relief, anti-allergic and respiratory medications', 1),
(6, 'Electrolytes & Rehydration', 'Oral rehydration salts and essential mineral electrolytes', 1),
(7, 'Vitamins & Supplements', 'Daily multivitamins, vitamin C, zinc, and dietary minerals', 1),
(8, 'Gastrointestinal', 'Antacids, proton pump inhibitors, and gastric acidity regulators', 1),
(9, 'Respiratory', 'Bronchodilators, cough suppressants, and respiratory inhalants', 1);

-- ----------------------------------------------------------------------------------------------------
-- 4. SEED SAMPLE MEDICINES (18 Items)
-- ----------------------------------------------------------------------------------------------------
INSERT IGNORE INTO medicines (
    id, category_id, name, generic_name, strength, dosage_form, 
    manufacturer, requires_prescription, is_active
) VALUES
(1, 1, 'Paracetamol 500mg', 'Paracetamol', '500 mg', 'Tablet', 'Cipla', 0, 1),
(2, 1, 'Paracetamol 650mg', 'Paracetamol', '650 mg', 'Tablet', 'Micro Labs', 0, 1),
(3, 5, 'Cetirizine 10mg', 'Cetirizine', '10 mg', 'Tablet', 'Mankind', 0, 1),
(4, 6, 'ORS Packets 21.8g', 'Oral Rehydration Salts', '21.8 g', 'Sachet', 'FDC Limited', 0, 1),
(5, 7, 'Vitamin C Tablets 500mg', 'Ascorbic Acid', '500 mg', 'Chewable Tablet', 'Abbott', 0, 1),
(6, 8, 'Omeprazole 20mg', 'Omeprazole', '20 mg', 'Capsule', 'Dr. Reddy\'s Laboratories', 1, 1),
(7, 8, 'Pantoprazole 40mg', 'Pantoprazole', '40 mg', 'Tablet', 'Alkem Laboratories', 1, 1),
(8, 2, 'Amoxicillin 500mg', 'Amoxicillin', '500 mg', 'Capsule', 'GlaxoSmithKline', 1, 1),
(9, 2, 'Azithromycin 250mg', 'Azithromycin', '250 mg', 'Tablet', 'Zydus Cadila', 1, 1),
(10, 3, 'Metformin 500mg', 'Metformin Hydrochloride', '500 mg', 'Tablet', 'USV Private Limited', 1, 1),
(11, 3, 'Glimepiride 2mg', 'Glimepiride', '2 mg', 'Tablet', 'Sanofi India', 1, 1),
(12, 4, 'Amlodipine 5mg', 'Amlodipine Besylate', '5 mg', 'Tablet', 'Torrent Pharmaceuticals', 1, 1),
(13, 4, 'Atorvastatin 10mg', 'Atorvastatin Calcium', '10 mg', 'Tablet', 'Sun Pharma', 1, 1),
(14, 1, 'Ibuprofen 400mg', 'Ibuprofen', '400 mg', 'Tablet', 'Abbott', 0, 1),
(15, 5, 'Montelukast 10mg', 'Montelukast Sodium', '10 mg', 'Tablet', 'Glenmark Pharmaceuticals', 1, 1),
(16, 7, 'Zinc Sulfate 50mg', 'Zinc Sulfate Monohydrate', '50 mg', 'Tablet', 'Apex Laboratories', 0, 1),
(17, 9, 'Dextromethorphan Syrup 100ml', 'Dextromethorphan Hydrobromide', '100 ml', 'Syrup', 'Dabur India', 0, 1),
(18, 3, 'Insulin Glargine 100 IU/ml', 'Insulin Glargine', '100 IU/ml', 'Injection', 'Sanofi India', 1, 1);

-- ----------------------------------------------------------------------------------------------------
-- 5. SEED REALISTIC MEDICINE INVENTORY (Pharmacy-Wise)
-- ----------------------------------------------------------------------------------------------------
-- Pharmacy 101: Apollo Pharmacy - T. Nagar (Primary Dashboard)
-- Covers IN_STOCK, LOW_STOCK (triggers reorder banner), OUT_OF_STOCK, and EXPIRING_SOON (<30 days alert).
INSERT INTO pharmacy_inventory (
    pharmacy_id, medicine_id, batch_no, quantity, reserved_quantity, 
    unit_price, low_stock_threshold, expiry_date, data_source, is_active
) VALUES
-- IN_STOCK Items
(101, 1, 'BATCH-P500-01', 75, 0, 20.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 14 MONTH), 'SAMPLE', 1),
(101, 2, 'BATCH-P650-01', 120, 5, 28.50, 15, DATE_ADD(CURRENT_DATE, INTERVAL 12 MONTH), 'SAMPLE', 1),
(101, 3, 'BATCH-CT10-01', 90, 0, 18.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 20 MONTH), 'SAMPLE', 1),
(101, 4, 'BATCH-ORS-01', 150, 0, 22.00, 20, DATE_ADD(CURRENT_DATE, INTERVAL 18 MONTH), 'SAMPLE', 1),
(101, 5, 'BATCH-VTC-01', 80, 0, 35.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 16 MONTH), 'SAMPLE', 1),
(101, 6, 'BATCH-OM20-01', 60, 2, 55.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 15 MONTH), 'SAMPLE', 1),
(101, 7, 'BATCH-PT40-01', 85, 0, 68.00, 12, DATE_ADD(CURRENT_DATE, INTERVAL 14 MONTH), 'SAMPLE', 1),
(101, 10, 'BATCH-MF500-01', 110, 0, 32.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 18 MONTH), 'SAMPLE', 1),
(101, 11, 'BATCH-GL02-01', 40, 0, 42.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 10 MONTH), 'SAMPLE', 1),
(101, 12, 'BATCH-AM05-01', 70, 0, 34.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 16 MONTH), 'SAMPLE', 1),
(101, 13, 'BATCH-AT10-01', 55, 0, 65.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 11 MONTH), 'SAMPLE', 1),
(101, 16, 'BATCH-ZN50-01', 95, 0, 42.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 22 MONTH), 'SAMPLE', 1),

-- LOW_STOCK Items (Available Quantity <= Low Stock Threshold)
(101, 8, 'BATCH-AM500-01', 6, 1, 45.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 9 MONTH), 'SAMPLE', 1),
(101, 15, 'BATCH-MON10-01', 5, 0, 98.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 8 MONTH), 'SAMPLE', 1),

-- OUT_OF_STOCK Items (Quantity = 0)
(101, 14, 'BATCH-IB400-01', 0, 0, 15.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 6 MONTH), 'SAMPLE', 1),
(101, 18, 'BATCH-INS-01', 0, 0, 650.00, 5, DATE_ADD(CURRENT_DATE, INTERVAL 8 MONTH), 'SAMPLE', 1),

-- EXPIRING_SOON Items (Expiry Date within 30 days)
(101, 9, 'BATCH-AZ250-01', 30, 0, 120.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 'SAMPLE', 1),
(101, 17, 'BATCH-DX100-01', 18, 0, 75.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY), 'SAMPLE', 1)
ON DUPLICATE KEY UPDATE
    quantity = VALUES(quantity),
    reserved_quantity = VALUES(reserved_quantity),
    unit_price = VALUES(unit_price),
    low_stock_threshold = VALUES(low_stock_threshold),
    expiry_date = VALUES(expiry_date),
    data_source = VALUES(data_source),
    is_active = 1;

-- Pharmacy 102: MedPlus - Anna Nagar
INSERT INTO pharmacy_inventory (pharmacy_id, medicine_id, batch_no, quantity, reserved_quantity, unit_price, low_stock_threshold, expiry_date, data_source, is_active) VALUES
(102, 1, 'AN-P500-01', 60, 0, 20.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 12 MONTH), 'SAMPLE', 1),
(102, 2, 'AN-P650-01', 90, 0, 29.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 11 MONTH), 'SAMPLE', 1),
(102, 3, 'AN-CT10-01', 70, 0, 18.50, 10, DATE_ADD(CURRENT_DATE, INTERVAL 15 MONTH), 'SAMPLE', 1),
(102, 4, 'AN-ORS-01', 100, 0, 22.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 16 MONTH), 'SAMPLE', 1),
(102, 6, 'AN-OM20-01', 50, 0, 54.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 14 MONTH), 'SAMPLE', 1),
(102, 10, 'AN-MF500-01', 85, 0, 32.50, 10, DATE_ADD(CURRENT_DATE, INTERVAL 13 MONTH), 'SAMPLE', 1)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit_price = VALUES(unit_price), expiry_date = VALUES(expiry_date), is_active = 1;

-- Pharmacy 108: Wellness Forever - Porur
INSERT INTO pharmacy_inventory (pharmacy_id, medicine_id, batch_no, quantity, reserved_quantity, unit_price, low_stock_threshold, expiry_date, data_source, is_active) VALUES
(108, 1, 'PO-P500-01', 100, 0, 20.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 15 MONTH), 'SAMPLE', 1),
(108, 2, 'PO-P650-01', 140, 2, 28.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 14 MONTH), 'SAMPLE', 1),
(108, 4, 'PO-ORS-01', 120, 0, 21.50, 20, DATE_ADD(CURRENT_DATE, INTERVAL 18 MONTH), 'SAMPLE', 1),
(108, 5, 'PO-VTC-01', 90, 0, 35.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 16 MONTH), 'SAMPLE', 1),
(108, 8, 'PO-AM500-01', 45, 0, 46.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 10 MONTH), 'SAMPLE', 1),
(108, 12, 'PO-AM05-01', 65, 0, 34.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 17 MONTH), 'SAMPLE', 1)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit_price = VALUES(unit_price), expiry_date = VALUES(expiry_date), is_active = 1;

-- Pharmacy 111: Apollo Pharmacy - Poonamallee Trunk Road (LIVE)
INSERT INTO pharmacy_inventory (pharmacy_id, medicine_id, batch_no, quantity, reserved_quantity, unit_price, low_stock_threshold, expiry_date, data_source, is_active) VALUES
(111, 1, 'LIVE-PN-P500', 95, 0, 20.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 13 MONTH), 'LIVE', 1),
(111, 2, 'LIVE-PN-P650', 120, 5, 29.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 10 MONTH), 'LIVE', 1),
(111, 3, 'LIVE-PN-CET10', 95, 0, 18.50, 15, DATE_ADD(CURRENT_DATE, INTERVAL 16 MONTH), 'LIVE', 1),
(111, 4, 'LIVE-PN-ORS', 130, 0, 22.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 18 MONTH), 'LIVE', 1),
(111, 5, 'LIVE-PN-VTC', 85, 0, 35.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 14 MONTH), 'LIVE', 1),
(111, 8, 'LIVE-PN-AM500', 45, 2, 48.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 8 MONTH), 'LIVE', 1),
(111, 9, 'LIVE-PN-AZ250', 30, 0, 125.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 6 MONTH), 'LIVE', 1),
(111, 12, 'LIVE-PN-AML05', 60, 4, 34.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 14 MONTH), 'LIVE', 1)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit_price = VALUES(unit_price), expiry_date = VALUES(expiry_date), is_active = 1;

-- Pharmacy 115: MedPlus - Iyyappanthangal (LIVE)
INSERT INTO pharmacy_inventory (pharmacy_id, medicine_id, batch_no, quantity, reserved_quantity, unit_price, low_stock_threshold, expiry_date, data_source, is_active) VALUES
(115, 1, 'LIVE-IY-P500', 80, 0, 19.50, 10, DATE_ADD(CURRENT_DATE, INTERVAL 12 MONTH), 'LIVE', 1),
(115, 2, 'LIVE-IY-P650', 85, 3, 29.50, 10, DATE_ADD(CURRENT_DATE, INTERVAL 12 MONTH), 'LIVE', 1),
(115, 3, 'LIVE-IY-CET10', 75, 0, 18.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 15 MONTH), 'LIVE', 1),
(115, 4, 'LIVE-IY-ORS', 110, 0, 22.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 17 MONTH), 'LIVE', 1),
(115, 11, 'LIVE-IY-GLM02', 55, 0, 42.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 10 MONTH), 'LIVE', 1),
(115, 15, 'LIVE-IY-MON10', 35, 1, 98.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 7 MONTH), 'LIVE', 1)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit_price = VALUES(unit_price), expiry_date = VALUES(expiry_date), is_active = 1;

-- Pharmacy 122: MedPlus - Ambattur OT
INSERT INTO pharmacy_inventory (pharmacy_id, medicine_id, batch_no, quantity, reserved_quantity, unit_price, low_stock_threshold, expiry_date, data_source, is_active) VALUES
(122, 1, 'AMB-P500-01', 70, 0, 20.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 11 MONTH), 'SAMPLE', 1),
(122, 2, 'AMB-P650-01', 85, 0, 28.50, 10, DATE_ADD(CURRENT_DATE, INTERVAL 12 MONTH), 'SAMPLE', 1),
(122, 3, 'AMB-CET10-01', 65, 0, 18.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 14 MONTH), 'SAMPLE', 1),
(122, 4, 'AMB-ORS-01', 90, 0, 22.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 16 MONTH), 'SAMPLE', 1),
(122, 10, 'AMB-MF500-01', 80, 0, 32.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 13 MONTH), 'SAMPLE', 1)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit_price = VALUES(unit_price), expiry_date = VALUES(expiry_date), is_active = 1;

-- Pharmacy 103: Apollo - Velachery
INSERT INTO pharmacy_inventory (pharmacy_id, medicine_id, batch_no, quantity, reserved_quantity, unit_price, low_stock_threshold, expiry_date, data_source, is_active) VALUES
(103, 1, 'VEL-P500-01', 85, 0, 20.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 14 MONTH), 'SAMPLE', 1),
(103, 2, 'VEL-P650-01', 110, 0, 28.50, 15, DATE_ADD(CURRENT_DATE, INTERVAL 12 MONTH), 'SAMPLE', 1),
(103, 4, 'VEL-ORS-01', 120, 0, 22.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 18 MONTH), 'SAMPLE', 1),
(103, 6, 'VEL-OM20-01', 50, 0, 55.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 15 MONTH), 'SAMPLE', 1)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit_price = VALUES(unit_price), expiry_date = VALUES(expiry_date), is_active = 1;

-- Pharmacy 106: MedPlus - Tambaram
INSERT INTO pharmacy_inventory (pharmacy_id, medicine_id, batch_no, quantity, reserved_quantity, unit_price, low_stock_threshold, expiry_date, data_source, is_active) VALUES
(106, 1, 'TAM-P500-01', 75, 0, 20.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 13 MONTH), 'SAMPLE', 1),
(106, 2, 'TAM-P650-01', 95, 0, 28.50, 10, DATE_ADD(CURRENT_DATE, INTERVAL 11 MONTH), 'SAMPLE', 1),
(106, 3, 'TAM-CET10-01', 60, 0, 18.00, 10, DATE_ADD(CURRENT_DATE, INTERVAL 16 MONTH), 'SAMPLE', 1),
(106, 4, 'TAM-ORS-01', 110, 0, 22.00, 15, DATE_ADD(CURRENT_DATE, INTERVAL 17 MONTH), 'SAMPLE', 1)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit_price = VALUES(unit_price), expiry_date = VALUES(expiry_date), is_active = 1;

-- ====================================================================================================
-- SEED SCRIPT COMPLETE
-- ====================================================================================================
