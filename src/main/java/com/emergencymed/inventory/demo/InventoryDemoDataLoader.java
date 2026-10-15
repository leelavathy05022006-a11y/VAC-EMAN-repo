package com.emergencymed.inventory.demo;

import com.emergencymed.auth.entity.Pharmacy;
import com.emergencymed.auth.repository.PharmacyRepository;
import com.emergencymed.common.enums.ApprovalStatus;
import com.emergencymed.inventory.entity.Medicine;
import com.emergencymed.inventory.entity.MedicineCategory;
import com.emergencymed.inventory.entity.PharmacyInventory;
import com.emergencymed.inventory.entity.PharmacySettings;
import com.emergencymed.inventory.repository.MedicineCategoryRepository;
import com.emergencymed.inventory.repository.MedicineRepository;
import com.emergencymed.inventory.repository.PharmacyInventoryRepository;
import com.emergencymed.inventory.repository.PharmacySettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Component
@Order(200)
public class InventoryDemoDataLoader implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(InventoryDemoDataLoader.class);

    private final MedicineCategoryRepository categoryRepository;
    private final MedicineRepository medicineRepository;
    private final PharmacyInventoryRepository inventoryRepository;
    private final PharmacySettingsRepository settingsRepository;
    private final PharmacyRepository pharmacyRepository;

    public InventoryDemoDataLoader(MedicineCategoryRepository categoryRepository,
                                   MedicineRepository medicineRepository,
                                   PharmacyInventoryRepository inventoryRepository,
                                   PharmacySettingsRepository settingsRepository,
                                   PharmacyRepository pharmacyRepository) {
        this.categoryRepository = categoryRepository;
        this.medicineRepository = medicineRepository;
        this.inventoryRepository = inventoryRepository;
        this.settingsRepository = settingsRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking and seeding Module 2 demo data (pharmacies, medicines, categories, settings, inventory)...");

        // 1. Ensure Categories
        MedicineCategory catAnalgesics = getOrCreateCategory("Analgesics & Antipyretics", "Pain relief, fever reducers and anti-inflammatory medications");
        MedicineCategory catAntihistamines = getOrCreateCategory("Antihistamines", "Allergy relief, anti-allergic and respiratory medications");
        MedicineCategory catElectrolytes = getOrCreateCategory("Electrolytes & Rehydration", "Oral rehydration salts and essential mineral electrolytes");
        MedicineCategory catVitamins = getOrCreateCategory("Vitamins & Supplements", "Daily multivitamins, vitamin C, zinc, and dietary minerals");
        MedicineCategory catGastro = getOrCreateCategory("Gastrointestinal", "Antacids, proton pump inhibitors, and gastric acidity regulators");
        MedicineCategory catAntibiotics = getOrCreateCategory("Antibiotics", "Broad-spectrum antibacterial and antimicrobial therapeutics");
        MedicineCategory catAntidiabetics = getOrCreateCategory("Antidiabetics", "Blood glucose regulators, oral hypoglycemics, and insulin");
        MedicineCategory catCardio = getOrCreateCategory("Cardiovascular", "Antihypertensives, cholesterol regulators, and cardiac support");
        MedicineCategory catRespiratory = getOrCreateCategory("Respiratory", "Bronchodilators, cough suppressants, and respiratory inhalants");

        // 2. Ensure Medicines (At least 18 sample medicines including required items)
        Medicine m1 = getOrCreateMedicine(catAnalgesics, "Paracetamol 500mg", "Paracetamol", "500 mg", "Tablet", "Cipla", false);
        Medicine m2 = getOrCreateMedicine(catAnalgesics, "Paracetamol 650mg", "Paracetamol", "650 mg", "Tablet", "Micro Labs", false);
        Medicine m3 = getOrCreateMedicine(catAntihistamines, "Cetirizine 10mg", "Cetirizine", "10 mg", "Tablet", "Mankind", false);
        Medicine m4 = getOrCreateMedicine(catElectrolytes, "ORS Packets 21.8g", "Oral Rehydration Salts", "21.8 g", "Sachet", "FDC Limited", false);
        Medicine m5 = getOrCreateMedicine(catVitamins, "Vitamin C Tablets 500mg", "Ascorbic Acid", "500 mg", "Chewable Tablet", "Abbott", false);
        Medicine m6 = getOrCreateMedicine(catGastro, "Omeprazole 20mg", "Omeprazole", "20 mg", "Capsule", "Dr. Reddy's Laboratories", true);
        Medicine m7 = getOrCreateMedicine(catGastro, "Pantoprazole 40mg", "Pantoprazole", "40 mg", "Tablet", "Alkem Laboratories", true);
        Medicine m8 = getOrCreateMedicine(catAntibiotics, "Amoxicillin 500mg", "Amoxicillin", "500 mg", "Capsule", "GlaxoSmithKline", true);
        Medicine m9 = getOrCreateMedicine(catAntibiotics, "Azithromycin 250mg", "Azithromycin", "250 mg", "Tablet", "Zydus Cadila", true);
        Medicine m10 = getOrCreateMedicine(catAntidiabetics, "Metformin 500mg", "Metformin Hydrochloride", "500 mg", "Tablet", "USV Private Limited", true);
        Medicine m11 = getOrCreateMedicine(catAntidiabetics, "Glimepiride 2mg", "Glimepiride", "2 mg", "Tablet", "Sanofi India", true);
        Medicine m12 = getOrCreateMedicine(catCardio, "Amlodipine 5mg", "Amlodipine Besylate", "5 mg", "Tablet", "Torrent Pharmaceuticals", true);
        Medicine m13 = getOrCreateMedicine(catCardio, "Atorvastatin 10mg", "Atorvastatin Calcium", "10 mg", "Tablet", "Sun Pharma", true);
        Medicine m14 = getOrCreateMedicine(catAnalgesics, "Ibuprofen 400mg", "Ibuprofen", "400 mg", "Tablet", "Abbott", false);
        Medicine m15 = getOrCreateMedicine(catAntihistamines, "Montelukast 10mg", "Montelukast Sodium", "10 mg", "Tablet", "Glenmark Pharmaceuticals", true);
        Medicine m16 = getOrCreateMedicine(catVitamins, "Zinc Sulfate 50mg", "Zinc Sulfate Monohydrate", "50 mg", "Tablet", "Apex Laboratories", false);
        Medicine m17 = getOrCreateMedicine(catRespiratory, "Dextromethorphan Syrup 100ml", "Dextromethorphan Hydrobromide", "100 ml", "Syrup", "Dabur India", false);
        Medicine m18 = getOrCreateMedicine(catAntidiabetics, "Insulin Glargine 100 IU/ml", "Insulin Glargine", "100 IU/ml", "Injection", "Sanofi India", true);

        // 3. Ensure 10 Chennai Pharmacies (Approval, Active, Delivery) & Settings
        Long[] pharmacyIds = { 101L, 102L, 103L, 104L, 106L, 107L, 108L, 111L, 115L, 122L };
        for (Long pid : pharmacyIds) {
            pharmacyRepository.findById(pid).ifPresent(p -> {
                p.setIsApproved(true);
                p.setApprovalStatus(ApprovalStatus.APPROVED);
                p.setActive(true);
                p.setDeliveryAvailable(true);
                pharmacyRepository.save(p);
            });
        }

        // Seed Pharmacy Settings (Delivery & Emergency)
        ensureSettings(101L, "044-24340055", "919876543210", true);   // T. Nagar
        ensureSettings(102L, "044-26211234", "919876543211", false);  // Anna Nagar
        ensureSettings(103L, "044-22435678", "919876543212", true);   // Velachery
        ensureSettings(104L, "044-24419876", "919876543213", false);  // Adyar
        ensureSettings(106L, "044-22263456", "919876543214", false);  // Tambaram
        ensureSettings(107L, "044-22348899", "919876543215", true);   // Guindy
        ensureSettings(108L, "044-24769988", "919876543216", true);   // Porur
        ensureSettings(111L, "044-26272211", "919876543217", true);   // Poonamallee
        ensureSettings(115L, "044-24765500", "919876543218", true);   // Iyyappanthangal
        ensureSettings(122L, "044-26581122", "919876543219", false);  // Ambattur

        // 4. Seed Inventory for Pharmacy 101 (Default Inventory Dashboard View)
        // IN_STOCK items
        ensureInventory(101L, m1, "BATCH-P500-01", 75, 0, new BigDecimal("20.00"), 10, LocalDate.now().plusMonths(14), "SAMPLE");
        ensureInventory(101L, m2, "BATCH-P650-01", 120, 5, new BigDecimal("28.50"), 15, LocalDate.now().plusMonths(12), "SAMPLE");
        ensureInventory(101L, m3, "BATCH-CT10-01", 90, 0, new BigDecimal("18.00"), 10, LocalDate.now().plusMonths(20), "SAMPLE");
        ensureInventory(101L, m4, "BATCH-ORS-01", 150, 0, new BigDecimal("22.00"), 20, LocalDate.now().plusMonths(18), "SAMPLE");
        ensureInventory(101L, m5, "BATCH-VTC-01", 80, 0, new BigDecimal("35.00"), 15, LocalDate.now().plusMonths(16), "SAMPLE");
        ensureInventory(101L, m6, "BATCH-OM20-01", 60, 2, new BigDecimal("55.00"), 10, LocalDate.now().plusMonths(15), "SAMPLE");
        ensureInventory(101L, m7, "BATCH-PT40-01", 85, 0, new BigDecimal("68.00"), 12, LocalDate.now().plusMonths(14), "SAMPLE");
        ensureInventory(101L, m10, "BATCH-MF500-01", 110, 0, new BigDecimal("32.00"), 15, LocalDate.now().plusMonths(18), "SAMPLE");
        ensureInventory(101L, m11, "BATCH-GL02-01", 40, 0, new BigDecimal("42.00"), 10, LocalDate.now().plusMonths(10), "SAMPLE");
        ensureInventory(101L, m12, "BATCH-AM05-01", 70, 0, new BigDecimal("34.00"), 10, LocalDate.now().plusMonths(16), "SAMPLE");
        ensureInventory(101L, m13, "BATCH-AT10-01", 55, 0, new BigDecimal("65.00"), 10, LocalDate.now().plusMonths(11), "SAMPLE");
        ensureInventory(101L, m16, "BATCH-ZN50-01", 95, 0, new BigDecimal("42.00"), 15, LocalDate.now().plusMonths(22), "SAMPLE");

        // LOW_STOCK items (triggers Smart Reorder Suggestions & Low Stock Banner)
        ensureInventory(101L, m8, "BATCH-AM500-01", 6, 1, new BigDecimal("45.00"), 10, LocalDate.now().plusMonths(9), "SAMPLE");
        ensureInventory(101L, m15, "BATCH-MON10-01", 5, 0, new BigDecimal("98.00"), 10, LocalDate.now().plusMonths(8), "SAMPLE");

        // OUT_OF_STOCK items
        ensureInventory(101L, m14, "BATCH-IB400-01", 0, 0, new BigDecimal("15.00"), 10, LocalDate.now().plusMonths(6), "SAMPLE");
        ensureInventory(101L, m18, "BATCH-INS-01", 0, 0, new BigDecimal("650.00"), 5, LocalDate.now().plusMonths(8), "SAMPLE");

        // EXPIRING_SOON items (within 30 days - triggers Expiry Alerts)
        ensureInventory(101L, m9, "BATCH-AZ250-01", 30, 0, new BigDecimal("120.00"), 10, LocalDate.now().plusDays(15), "SAMPLE");
        ensureInventory(101L, m17, "BATCH-DX100-01", 18, 0, new BigDecimal("75.00"), 10, LocalDate.now().plusDays(20), "SAMPLE");

        // 5. Seed Inventory for other key Chennai Pharmacies
        // Pharmacy 102 - Anna Nagar
        ensureInventory(102L, m1, "AN-P500-01", 60, 0, new BigDecimal("20.00"), 10, LocalDate.now().plusMonths(12), "SAMPLE");
        ensureInventory(102L, m2, "AN-P650-01", 90, 0, new BigDecimal("29.00"), 10, LocalDate.now().plusMonths(11), "SAMPLE");
        ensureInventory(102L, m3, "AN-CT10-01", 70, 0, new BigDecimal("18.50"), 10, LocalDate.now().plusMonths(15), "SAMPLE");
        ensureInventory(102L, m4, "AN-ORS-01", 100, 0, new BigDecimal("22.00"), 15, LocalDate.now().plusMonths(16), "SAMPLE");
        ensureInventory(102L, m6, "AN-OM20-01", 50, 0, new BigDecimal("54.00"), 10, LocalDate.now().plusMonths(14), "SAMPLE");
        ensureInventory(102L, m10, "AN-MF500-01", 85, 0, new BigDecimal("32.50"), 10, LocalDate.now().plusMonths(13), "SAMPLE");

        // Pharmacy 108 - Porur (Wellness Forever)
        ensureInventory(108L, m1, "PO-P500-01", 100, 0, new BigDecimal("20.00"), 15, LocalDate.now().plusMonths(15), "SAMPLE");
        ensureInventory(108L, m2, "PO-P650-01", 140, 2, new BigDecimal("28.00"), 15, LocalDate.now().plusMonths(14), "SAMPLE");
        ensureInventory(108L, m4, "PO-ORS-01", 120, 0, new BigDecimal("21.50"), 20, LocalDate.now().plusMonths(18), "SAMPLE");
        ensureInventory(108L, m5, "PO-VTC-01", 90, 0, new BigDecimal("35.00"), 15, LocalDate.now().plusMonths(16), "SAMPLE");
        ensureInventory(108L, m8, "PO-AM500-01", 45, 0, new BigDecimal("46.00"), 10, LocalDate.now().plusMonths(10), "SAMPLE");
        ensureInventory(108L, m12, "PO-AM05-01", 65, 0, new BigDecimal("34.00"), 10, LocalDate.now().plusMonths(17), "SAMPLE");

        // Pharmacy 111 - Poonamallee (Apollo Trunk Road - Live stock)
        ensureInventory(111L, m1, "LIVE-PN-P500", 95, 0, new BigDecimal("20.00"), 10, LocalDate.now().plusMonths(13), "LIVE");
        ensureInventory(111L, m2, "LIVE-PN-P650", 120, 5, new BigDecimal("29.00"), 15, LocalDate.now().plusMonths(10), "LIVE");
        ensureInventory(111L, m3, "LIVE-PN-CET10", 95, 0, new BigDecimal("18.50"), 15, LocalDate.now().plusMonths(16), "LIVE");
        ensureInventory(111L, m4, "LIVE-PN-ORS", 130, 0, new BigDecimal("22.00"), 15, LocalDate.now().plusMonths(18), "LIVE");
        ensureInventory(111L, m5, "LIVE-PN-VTC", 85, 0, new BigDecimal("35.00"), 10, LocalDate.now().plusMonths(14), "LIVE");
        ensureInventory(111L, m8, "LIVE-PN-AM500", 45, 2, new BigDecimal("48.00"), 10, LocalDate.now().plusMonths(8), "LIVE");
        ensureInventory(111L, m9, "LIVE-PN-AZ250", 30, 0, new BigDecimal("125.00"), 10, LocalDate.now().plusMonths(6), "LIVE");
        ensureInventory(111L, m12, "LIVE-PN-AML05", 60, 4, new BigDecimal("34.00"), 10, LocalDate.now().plusMonths(14), "LIVE");

        // Pharmacy 115 - Iyyappanthangal (MedPlus)
        ensureInventory(115L, m1, "LIVE-IY-P500", 80, 0, new BigDecimal("19.50"), 10, LocalDate.now().plusMonths(12), "LIVE");
        ensureInventory(115L, m2, "LIVE-IY-P650", 85, 3, new BigDecimal("29.50"), 10, LocalDate.now().plusMonths(12), "LIVE");
        ensureInventory(115L, m3, "LIVE-IY-CET10", 75, 0, new BigDecimal("18.00"), 10, LocalDate.now().plusMonths(15), "LIVE");
        ensureInventory(115L, m4, "LIVE-IY-ORS", 110, 0, new BigDecimal("22.00"), 15, LocalDate.now().plusMonths(17), "LIVE");
        ensureInventory(115L, m11, "LIVE-IY-GLM02", 55, 0, new BigDecimal("42.00"), 10, LocalDate.now().plusMonths(10), "LIVE");
        ensureInventory(115L, m15, "LIVE-IY-MON10", 35, 1, new BigDecimal("98.00"), 10, LocalDate.now().plusMonths(7), "LIVE");

        // Pharmacy 122 - Ambattur OT (MedPlus)
        ensureInventory(122L, m1, "AMB-P500-01", 70, 0, new BigDecimal("20.00"), 10, LocalDate.now().plusMonths(11), "SAMPLE");
        ensureInventory(122L, m2, "AMB-P650-01", 85, 0, new BigDecimal("28.50"), 10, LocalDate.now().plusMonths(12), "SAMPLE");
        ensureInventory(122L, m3, "AMB-CET10-01", 65, 0, new BigDecimal("18.00"), 10, LocalDate.now().plusMonths(14), "SAMPLE");
        ensureInventory(122L, m4, "AMB-ORS-01", 90, 0, new BigDecimal("22.00"), 15, LocalDate.now().plusMonths(16), "SAMPLE");
        ensureInventory(122L, m10, "AMB-MF500-01", 80, 0, new BigDecimal("32.00"), 10, LocalDate.now().plusMonths(13), "SAMPLE");

        // Pharmacy 103 - Velachery
        ensureInventory(103L, m1, "VEL-P500-01", 85, 0, new BigDecimal("20.00"), 10, LocalDate.now().plusMonths(14), "SAMPLE");
        ensureInventory(103L, m2, "VEL-P650-01", 110, 0, new BigDecimal("28.50"), 15, LocalDate.now().plusMonths(12), "SAMPLE");
        ensureInventory(103L, m4, "VEL-ORS-01", 120, 0, new BigDecimal("22.00"), 15, LocalDate.now().plusMonths(18), "SAMPLE");
        ensureInventory(103L, m6, "VEL-OM20-01", 50, 0, new BigDecimal("55.00"), 10, LocalDate.now().plusMonths(15), "SAMPLE");

        // Pharmacy 106 - Tambaram
        ensureInventory(106L, m1, "TAM-P500-01", 75, 0, new BigDecimal("20.00"), 10, LocalDate.now().plusMonths(13), "SAMPLE");
        ensureInventory(106L, m2, "TAM-P650-01", 95, 0, new BigDecimal("28.50"), 10, LocalDate.now().plusMonths(11), "SAMPLE");
        ensureInventory(106L, m3, "TAM-CET10-01", 60, 0, new BigDecimal("18.00"), 10, LocalDate.now().plusMonths(16), "SAMPLE");
        ensureInventory(106L, m4, "TAM-ORS-01", 110, 0, new BigDecimal("22.00"), 15, LocalDate.now().plusMonths(17), "SAMPLE");

        log.info("Module 2 demo data successfully seeded and verified across all pharmacies!");
    }

    private MedicineCategory getOrCreateCategory(String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new MedicineCategory(name, description, true)));
    }

    private Medicine getOrCreateMedicine(MedicineCategory category, String name, String genericName,
                                         String strength, String dosageForm, String manufacturer,
                                         boolean requiresPrescription) {
        return medicineRepository.findByNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCase(name, strength, dosageForm)
                .orElseGet(() -> medicineRepository.save(new Medicine(category, name, genericName, strength, dosageForm, manufacturer, requiresPrescription, true)));
    }

    private void ensureSettings(Long pharmacyId, String phone, String whatsapp, boolean opens24x7) {
        Optional<PharmacySettings> opt = settingsRepository.findByPharmacyId(pharmacyId);
        PharmacySettings s = opt.orElseGet(PharmacySettings::new);
        s.setPharmacyId(pharmacyId);
        s.setContactPhone(phone);
        s.setWhatsappNumber(whatsapp);
        s.setDeliveryEnabled(true);
        s.setDeliveryRadiusKm(new BigDecimal("10.0"));
        s.setDeliveryFee(new BigDecimal("30.00"));
        s.setOpens24x7(opens24x7);
        s.setOpeningTime(LocalTime.of(8, 0));
        s.setClosingTime(LocalTime.of(22, 0));
        s.setAcceptsEmergencyOrders(true);
        settingsRepository.save(s);
    }

    private void ensureInventory(Long pharmacyId, Medicine med, String batchNo, int qty, int res,
                                BigDecimal price, int threshold, LocalDate expiry, String dataSource) {
        Optional<PharmacyInventory> existing = inventoryRepository.findByPharmacyIdAndMedicineId(pharmacyId, med.getId());
        if (existing.isPresent()) {
            PharmacyInventory inv = existing.get();
            inv.setBatchNo(batchNo);
            inv.setQuantity(qty);
            inv.setReservedQuantity(res);
            inv.setUnitPrice(price);
            inv.setLowStockThreshold(threshold);
            inv.setExpiryDate(expiry);
            inv.setDataSource(dataSource);
            inv.setIsActive(true);
            inv.setLastUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inv);
        } else {
            PharmacyInventory inv = new PharmacyInventory();
            inv.setPharmacyId(pharmacyId);
            inv.setMedicine(med);
            inv.setBatchNo(batchNo);
            inv.setQuantity(qty);
            inv.setReservedQuantity(res);
            inv.setUnitPrice(price);
            inv.setLowStockThreshold(threshold);
            inv.setExpiryDate(expiry);
            inv.setDataSource(dataSource);
            inv.setIsActive(true);
            inv.setLastUpdatedAt(LocalDateTime.now().minusDays(1));
            inventoryRepository.save(inv);
        }
    }
}
