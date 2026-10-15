package com.emergencymed.inventory.service.impl;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.InventoryService;
import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.inventory.entity.InventoryTransaction;
import com.emergencymed.inventory.entity.PharmacyInventory;
import com.emergencymed.inventory.repository.InventoryTransactionRepository;
import com.emergencymed.inventory.exception.InsufficientStockException;
import com.emergencymed.inventory.exception.ResourceNotFoundException;
import com.emergencymed.inventory.repository.PharmacyInventoryRepository;
import com.emergencymed.inventory.service.StockStatusCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {
    private final PharmacyInventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final StockStatusCalculator statusCalculator;
    private final NotificationPublisher notificationPublisher;
    private final CurrentUserProvider currentUserProvider;

    public InventoryServiceImpl(PharmacyInventoryRepository inventoryRepository,
                                InventoryTransactionRepository transactionRepository,
                                StockStatusCalculator statusCalculator,
                                NotificationPublisher notificationPublisher,
                                CurrentUserProvider currentUserProvider) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.statusCalculator = statusCalculator;
        this.notificationPublisher = notificationPublisher;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public Long reserve(Long pharmacyId, Long medicineId, int qty, String refCode) {
        PharmacyInventory inv = inventoryRepository.findByPharmacyIdAndMedicineIdForUpdate(pharmacyId, medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found for pharmacy ID " + pharmacyId + " and medicine ID " + medicineId));

        // Idempotency check
        if (refCode != null && !refCode.isBlank()) {
            Optional<InventoryTransaction> existing = transactionRepository.findByInventoryIdAndReferenceCodeAndTxnType(inv.getId(), refCode, "RESERVE");
            if (existing.isPresent()) {
                return inv.getId();
            }
        }

        int available = inv.getAvailableQuantity();
        if (available < qty) {
            throw new InsufficientStockException("Insufficient available stock. Requested: " + qty + ", Available: " + available);
        }

        inv.setReservedQuantity(inv.getReservedQuantity() + qty);
        PharmacyInventory saved = inventoryRepository.save(inv);

        recordTxn(saved, "RESERVE", 0, saved.getQuantity(), saved.getReservedQuantity(), saved.getBatchNo(), refCode, "Reserved for request " + refCode);
        checkAndNotifyLowStock(saved);

        return saved.getId();
    }

    @Override
    public void commit(Long inventoryId, int qty, String refCode) {
        PharmacyInventory inv = inventoryRepository.findByIdForUpdate(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found with ID: " + inventoryId));

        // Idempotency check
        if (refCode != null && !refCode.isBlank()) {
            Optional<InventoryTransaction> existing = transactionRepository.findByInventoryIdAndReferenceCodeAndTxnType(inv.getId(), refCode, "COMMIT");
            if (existing.isPresent()) {
                return;
            }
        }

        int newQty = Math.max(0, inv.getQuantity() - qty);
        int newReserved = Math.max(0, inv.getReservedQuantity() - qty);

        inv.setQuantity(newQty);
        inv.setReservedQuantity(newReserved);
        PharmacyInventory saved = inventoryRepository.save(inv);

        recordTxn(saved, "COMMIT", -qty, saved.getQuantity(), saved.getReservedQuantity(), saved.getBatchNo(), refCode, "Committed for request " + refCode);
        checkAndNotifyLowStock(saved);
    }

    @Override
    public void release(Long inventoryId, int qty, String refCode) {
        PharmacyInventory inv = inventoryRepository.findByIdForUpdate(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found with ID: " + inventoryId));

        // Idempotency check
        if (refCode != null && !refCode.isBlank()) {
            Optional<InventoryTransaction> existing = transactionRepository.findByInventoryIdAndReferenceCodeAndTxnType(inv.getId(), refCode, "RELEASE");
            if (existing.isPresent()) {
                return;
            }
        }

        int newReserved = Math.max(0, inv.getReservedQuantity() - qty);
        inv.setReservedQuantity(newReserved);
        PharmacyInventory saved = inventoryRepository.save(inv);

        recordTxn(saved, "RELEASE", 0, saved.getQuantity(), saved.getReservedQuantity(), saved.getBatchNo(), refCode, "Released for request " + refCode);
        checkAndNotifyLowStock(saved);
    }

    @Override
    public void restore(Long inventoryId, int qty, String refCode) {
        PharmacyInventory inv = inventoryRepository.findByIdForUpdate(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory row not found with ID: " + inventoryId));

        // Idempotency check
        if (refCode != null && !refCode.isBlank()) {
            Optional<InventoryTransaction> existing = transactionRepository.findByInventoryIdAndReferenceCodeAndTxnType(inv.getId(), refCode, "RESTORE");
            if (existing.isPresent()) {
                return;
            }
        }

        inv.setQuantity(inv.getQuantity() + qty);
        PharmacyInventory saved = inventoryRepository.save(inv);

        recordTxn(saved, "RESTORE", qty, saved.getQuantity(), saved.getReservedQuantity(), saved.getBatchNo(), refCode, "Restored for request " + refCode);
        checkAndNotifyLowStock(saved);
    }

    private void recordTxn(PharmacyInventory inv, String txnType, int change, int qtyAfter, int resAfter, String batchNo, String refCode, String remarks) {
        Long userId = currentUserProvider.isAuthenticated() ? currentUserProvider.userId() : null;
        InventoryTransaction txn = new InventoryTransaction(inv, txnType, change, qtyAfter, resAfter, batchNo, refCode, userId, remarks);
        transactionRepository.save(txn);
    }

    private void checkAndNotifyLowStock(PharmacyInventory inv) {
        String status = statusCalculator.computeStatus(inv.getAvailableQuantity(), inv.getLowStockThreshold(), inv.getExpiryDate());
        if (StockStatusCalculator.LOW_STOCK.equals(status) || StockStatusCalculator.OUT_OF_STOCK.equals(status)) {
            Long userId = currentUserProvider.isAuthenticated() ? currentUserProvider.userId() : inv.getPharmacyId();
            notificationPublisher.publish(
                    userId,
                    "LOW_STOCK",
                    "Low Stock Alert: " + inv.getMedicine().getName(),
                    "Medicine " + inv.getMedicine().getName() + " is low/out of stock. Available: " + inv.getAvailableQuantity(),
                    null
            );
        }
    }
}
