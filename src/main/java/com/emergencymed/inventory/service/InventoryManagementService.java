package com.emergencymed.inventory.service;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.inventory.dto.*;
import com.emergencymed.inventory.entity.InventoryTransaction;
import com.emergencymed.inventory.entity.Medicine;
import com.emergencymed.inventory.entity.PharmacyInventory;
import com.emergencymed.inventory.repository.InventoryTransactionRepository;
import com.emergencymed.inventory.exception.*;
import com.emergencymed.inventory.repository.MedicineRepository;
import com.emergencymed.inventory.repository.PharmacyInventoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryManagementService {
    private final PharmacyInventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final MedicineRepository medicineRepository;
    private final StockStatusCalculator statusCalculator;
    private final CurrentUserProvider currentUserProvider;
    private final PharmacyLookup pharmacyLookup;
    private final NotificationPublisher notificationPublisher;

    public InventoryManagementService(PharmacyInventoryRepository inventoryRepository,
                                       InventoryTransactionRepository transactionRepository,
                                       MedicineRepository medicineRepository,
                                       StockStatusCalculator statusCalculator,
                                       CurrentUserProvider currentUserProvider,
                                       PharmacyLookup pharmacyLookup,
                                       NotificationPublisher notificationPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.medicineRepository = medicineRepository;
        this.statusCalculator = statusCalculator;
        this.currentUserProvider = currentUserProvider;
        this.pharmacyLookup = pharmacyLookup;
        this.notificationPublisher = notificationPublisher;
    }

    private Long verifyPharmacyAccess() {
        Long pharmacyId = currentUserProvider.pharmacyId()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Session missing pharmacy ID"));
        if (!pharmacyLookup.isApproved(pharmacyId)) {
            throw new PharmacyNotApprovedException("Pharmacy ID " + pharmacyId + " is not approved or active");
        }
        return pharmacyId;
    }

    @Transactional(readOnly = true)
    public Page<InventoryRowResponse> getInventory(String q, String status, Pageable pageable) {
        return getInventory(null, q, status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<InventoryRowResponse> getInventory(Long pharmacyId, String q, String status, Pageable pageable) {
        Long targetPharmacyId = pharmacyId != null ? pharmacyId : currentUserProvider.pharmacyId().orElse(101L);

        List<PharmacyInventory> allRows = inventoryRepository.findByPharmacyIdAndIsActiveTrue(targetPharmacyId);
        List<InventoryRowResponse> filtered = allRows.stream()
                .map(this::toResponse)
                .filter(row -> {
                    if (q != null && !q.isBlank()) {
                        String queryLower = q.toLowerCase();
                        boolean matchName = row.medicineName().toLowerCase().contains(queryLower);
                        boolean matchGeneric = row.genericName().toLowerCase().contains(queryLower);
                        if (!matchName && !matchGeneric) return false;
                    }
                    if (status != null && !status.isBlank()) {
                        if (status.equalsIgnoreCase("EXPIRING_SOON")) {
                            if (!Boolean.TRUE.equals(row.expiringWithin30Days())) return false;
                        } else {
                            if (!row.stockStatus().equalsIgnoreCase(status)) return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        if (start >= filtered.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, filtered.size());
        }
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public InventoryRowResponse getInventoryById(Long id) {
        Long pharmacyId = currentUserProvider.pharmacyId()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Session missing pharmacy ID"));
        PharmacyInventory inventory = inventoryRepository.findByPharmacyIdAndId(pharmacyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found for ID: " + id));
        return toResponse(inventory);
    }

    public InventoryRowResponse addStock(AddInventoryRequest req) {
        Long pharmacyId = verifyPharmacyAccess();

        if (req.expiryDate() != null && !req.expiryDate().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Expiry date must be after today");
        }

        if (inventoryRepository.existsByPharmacyIdAndMedicineId(pharmacyId, req.medicineId())) {
            throw new DuplicateResourceException("Pharmacy already has an inventory record for this medicine. Use Stock-in to add quantity.");
        }

        Medicine medicine = medicineRepository.findById(req.medicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + req.medicineId()));

        PharmacyInventory inv = new PharmacyInventory();
        inv.setPharmacyId(pharmacyId);
        inv.setMedicine(medicine);
        inv.setBatchNo(req.batchNo());
        inv.setQuantity(req.quantity());
        inv.setReservedQuantity(0);
        inv.setUnitPrice(req.unitPrice());
        inv.setLowStockThreshold(req.lowStockThreshold() != null ? req.lowStockThreshold() : 10);
        inv.setExpiryDate(req.expiryDate());
        inv.setDataSource("LIVE");
        inv.setIsActive(true);
        inv.setLastUpdatedAt(LocalDateTime.now());

        PharmacyInventory saved = inventoryRepository.save(inv);

        recordTransaction(saved, "STOCK_IN", req.quantity(), saved.getQuantity(), saved.getReservedQuantity(), req.batchNo(), null, "Initial stock-in");
        checkAndNotifyLowStock(saved);

        return toResponse(saved);
    }

    public InventoryRowResponse updateInventory(Long id, UpdateInventoryRequest req) {
        Long pharmacyId = verifyPharmacyAccess();

        PharmacyInventory inv = inventoryRepository.findByPharmacyIdAndId(pharmacyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found with ID: " + id));

        if (req.expiryDate() != null && !req.expiryDate().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Expiry date must be after today");
        }

        if (req.batchNo() != null) inv.setBatchNo(req.batchNo());
        if (req.unitPrice() != null) inv.setUnitPrice(req.unitPrice());
        if (req.lowStockThreshold() != null) inv.setLowStockThreshold(req.lowStockThreshold());
        if (req.expiryDate() != null) inv.setExpiryDate(req.expiryDate());
        inv.setDataSource("LIVE");
        inv.setLastUpdatedAt(LocalDateTime.now());

        PharmacyInventory saved = inventoryRepository.save(inv);
        checkAndNotifyLowStock(saved);
        return toResponse(saved);
    }

    public InventoryRowResponse stockIn(Long id, StockInRequest req) {
        Long pharmacyId = verifyPharmacyAccess();

        PharmacyInventory inv = inventoryRepository.findByPharmacyIdAndId(pharmacyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found with ID: " + id));

        if (req.expiryDate() != null && !req.expiryDate().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Expiry date must be after today");
        }

        int addQty = req.quantity();
        inv.setQuantity(inv.getQuantity() + addQty);
        if (req.batchNo() != null && !req.batchNo().isBlank()) {
            inv.setBatchNo(req.batchNo());
        }
        if (req.expiryDate() != null) {
            inv.setExpiryDate(req.expiryDate());
        }
        inv.setDataSource("LIVE");
        inv.setLastUpdatedAt(LocalDateTime.now());

        PharmacyInventory saved = inventoryRepository.save(inv);
        recordTransaction(saved, "STOCK_IN", addQty, saved.getQuantity(), saved.getReservedQuantity(), saved.getBatchNo(), null, "Stock-in addition");
        checkAndNotifyLowStock(saved);
        return toResponse(saved);
    }

    public InventoryRowResponse adjustStock(Long id, AdjustStockRequest req) {
        Long pharmacyId = verifyPharmacyAccess();

        PharmacyInventory inv = inventoryRepository.findByPharmacyIdAndId(pharmacyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found with ID: " + id));

        int oldQty = inv.getQuantity();
        int newQty = req.newQuantity();
        int diff = newQty - oldQty;

        inv.setQuantity(newQty);
        inv.setDataSource("LIVE");
        inv.setLastUpdatedAt(LocalDateTime.now());

        PharmacyInventory saved = inventoryRepository.save(inv);
        recordTransaction(saved, "ADJUSTMENT", diff, saved.getQuantity(), saved.getReservedQuantity(), saved.getBatchNo(), null, req.reason());
        checkAndNotifyLowStock(saved);
        return toResponse(saved);
    }

    public InventoryRowResponse confirmSampleRow(Long id) {
        Long pharmacyId = verifyPharmacyAccess();

        PharmacyInventory inv = inventoryRepository.findByPharmacyIdAndId(pharmacyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found with ID: " + id));

        inv.setDataSource("LIVE");
        inv.setLastUpdatedAt(LocalDateTime.now());

        PharmacyInventory saved = inventoryRepository.save(inv);
        return toResponse(saved);
    }

    public void removeInventory(Long id) {
        Long pharmacyId = verifyPharmacyAccess();

        PharmacyInventory inv = inventoryRepository.findByPharmacyIdAndId(pharmacyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found with ID: " + id));

        if (inv.getReservedQuantity() != null && inv.getReservedQuantity() > 0) {
            throw new InvalidStateTransitionException("Cannot delete inventory row with active reservations (reserved_quantity: " + inv.getReservedQuantity() + ")");
        }

        inv.setIsActive(false);
        inventoryRepository.save(inv);
    }

    @Transactional(readOnly = true)
    public InventorySummaryDto getSummary() {
        return getSummary(null);
    }

    @Transactional(readOnly = true)
    public InventorySummaryDto getSummary(Long pharmacyId) {
        Long targetId = pharmacyId != null ? pharmacyId : currentUserProvider.pharmacyId().orElse(101L);

        List<PharmacyInventory> list = inventoryRepository.findByPharmacyIdAndIsActiveTrue(targetId);
        long total = list.size();
        long inStock = 0;
        long lowStock = 0;
        long outOfStock = 0;
        long expiring = 0;

        for (PharmacyInventory row : list) {
            String st = statusCalculator.computeStatus(row.getAvailableQuantity(), row.getLowStockThreshold(), row.getExpiryDate());
            if (st.equals(StockStatusCalculator.IN_STOCK)) inStock++;
            else if (st.equals(StockStatusCalculator.LOW_STOCK)) lowStock++;
            else if (st.equals(StockStatusCalculator.OUT_OF_STOCK)) outOfStock++;

            if (statusCalculator.isExpiringWithin30Days(row.getExpiryDate())) {
                expiring++;
            }
        }

        return new InventorySummaryDto(total, inStock, lowStock, outOfStock, expiring);
    }

    @Transactional(readOnly = true)
    public List<InventoryRowResponse> getAlerts() {
        return getAlerts(null);
    }

    @Transactional(readOnly = true)
    public List<InventoryRowResponse> getAlerts(Long pharmacyId) {
        Long targetId = pharmacyId != null ? pharmacyId : currentUserProvider.pharmacyId().orElse(101L);

        List<PharmacyInventory> list = inventoryRepository.findByPharmacyIdAndIsActiveTrue(targetId);
        return list.stream()
                .map(this::toResponse)
                .filter(row -> row.stockStatus().equals(StockStatusCalculator.LOW_STOCK) ||
                               row.stockStatus().equals(StockStatusCalculator.OUT_OF_STOCK) ||
                               row.stockStatus().equals(StockStatusCalculator.EXPIRED) ||
                               Boolean.TRUE.equals(row.expiringWithin30Days()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<InventoryTransactionDto> getTransactions(Long inventoryId, Pageable pageable) {
        Long pharmacyId = currentUserProvider.pharmacyId()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Session missing pharmacy ID"));

        PharmacyInventory inv = inventoryRepository.findByPharmacyIdAndId(pharmacyId, inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found with ID: " + inventoryId));

        return transactionRepository.findByInventoryIdOrderByCreatedAtDesc(inv.getId(), pageable)
                .map(t -> new InventoryTransactionDto(
                        t.getId(),
                        t.getInventory().getId(),
                        t.getTxnType(),
                        t.getQuantityChange(),
                        t.getQuantityAfter(),
                        t.getReservedAfter(),
                        t.getBatchNo(),
                        t.getReferenceCode(),
                        t.getPerformedByUserId(),
                        t.getRemarks(),
                        t.getCreatedAt().toString()
                ));
    }

    @Transactional(readOnly = true)
    public Page<InventoryRowResponse> getAdminInventoryByPharmacy(Long pharmacyId, Pageable pageable) {
        List<PharmacyInventory> list = inventoryRepository.findByPharmacyIdAndIsActiveTrue(pharmacyId);
        List<InventoryRowResponse> responses = list.stream().map(this::toResponse).collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        if (start >= responses.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, responses.size());
        }
        int end = Math.min(start + pageable.getPageSize(), responses.size());
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    private void recordTransaction(PharmacyInventory inv, String txnType, int change, int qtyAfter, int resAfter, String batchNo, String refCode, String remarks) {
        Long userId = currentUserProvider.userId();
        InventoryTransaction txn = new InventoryTransaction(inv, txnType, change, qtyAfter, resAfter, batchNo, refCode, userId, remarks);
        transactionRepository.save(txn);
    }

    private void checkAndNotifyLowStock(PharmacyInventory inv) {
        String status = statusCalculator.computeStatus(inv.getAvailableQuantity(), inv.getLowStockThreshold(), inv.getExpiryDate());
        if (StockStatusCalculator.LOW_STOCK.equals(status) || StockStatusCalculator.OUT_OF_STOCK.equals(status)) {
            Long userId = currentUserProvider.userId();
            notificationPublisher.publish(
                    userId,
                    "LOW_STOCK",
                    "Low Stock Alert: " + inv.getMedicine().getName(),
                    "Medicine " + inv.getMedicine().getName() + " (Batch " + inv.getBatchNo() + ") is low/out of stock. Available: " + inv.getAvailableQuantity(),
                    null
            );
        }
    }

    public InventoryRowResponse toResponse(PharmacyInventory inv) {
        String status = statusCalculator.computeStatus(inv.getAvailableQuantity(), inv.getLowStockThreshold(), inv.getExpiryDate());
        boolean expiringSoon = statusCalculator.isExpiringWithin30Days(inv.getExpiryDate());

        return new InventoryRowResponse(
                inv.getId(),
                inv.getPharmacyId(),
                inv.getMedicine().getId(),
                inv.getMedicine().getName(),
                inv.getMedicine().getGenericName(),
                inv.getMedicine().getStrength(),
                inv.getMedicine().getDosageForm(),
                inv.getMedicine().getCategory().getName(),
                inv.getMedicine().getRequiresPrescription(),
                inv.getBatchNo(),
                inv.getQuantity(),
                inv.getReservedQuantity(),
                inv.getAvailableQuantity(),
                inv.getUnitPrice(),
                inv.getLowStockThreshold(),
                inv.getExpiryDate(),
                status,
                expiringSoon,
                inv.getDataSource(),
                inv.getIsActive(),
                inv.getLastUpdatedAt() != null ? inv.getLastUpdatedAt().toString() : null,
                inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : null
        );
    }
}
