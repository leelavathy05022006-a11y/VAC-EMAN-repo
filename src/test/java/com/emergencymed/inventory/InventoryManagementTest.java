package com.emergencymed.inventory;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.inventory.entity.InventoryTransaction;
import com.emergencymed.inventory.entity.Medicine;
import com.emergencymed.inventory.entity.PharmacyInventory;
import com.emergencymed.inventory.exception.InsufficientStockException;
import com.emergencymed.inventory.repository.InventoryTransactionRepository;
import com.emergencymed.inventory.repository.PharmacyInventoryRepository;
import com.emergencymed.inventory.service.StockStatusCalculator;
import com.emergencymed.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InventoryManagementTest {

    private PharmacyInventoryRepository inventoryRepository;
    private InventoryTransactionRepository transactionRepository;
    private NotificationPublisher notificationPublisher;
    private CurrentUserProvider currentUserProvider;
    private StockStatusCalculator statusCalculator;
    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(PharmacyInventoryRepository.class);
        transactionRepository = mock(InventoryTransactionRepository.class);
        notificationPublisher = mock(NotificationPublisher.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        statusCalculator = new StockStatusCalculator();

        inventoryService = new InventoryServiceImpl(
                inventoryRepository,
                transactionRepository,
                statusCalculator,
                notificationPublisher,
                currentUserProvider
        );
    }

    private PharmacyInventory createSampleInventory(Long id, int qty, int reserved) {
        PharmacyInventory inv = new PharmacyInventory();
        inv.setId(id);
        inv.setPharmacyId(1L);
        Medicine med = new Medicine();
        med.setId(10L);
        inv.setMedicine(med);
        inv.setQuantity(qty);
        inv.setReservedQuantity(reserved);
        inv.setBatchNo("BATCH-01");
        inv.setExpiryDate(LocalDate.now().plusMonths(6));
        return inv;
    }

    @Test
    @DisplayName("Stock status calculator correctly evaluates EXPIRED, OUT_OF_STOCK, LOW_STOCK, and IN_STOCK")
    void testStockStatusCalculations() {
        LocalDate today = LocalDate.now();

        // 1. Expired today or earlier
        assertEquals("EXPIRED", statusCalculator.computeStatus(50, 10, today));
        assertEquals("EXPIRED", statusCalculator.computeStatus(50, 10, today.minusDays(5)));

        // 2. Out of stock (0 available)
        assertEquals("OUT_OF_STOCK", statusCalculator.computeStatus(0, 10, today.plusDays(30)));

        // 3. Low stock (at or below threshold)
        assertEquals("LOW_STOCK", statusCalculator.computeStatus(10, 10, today.plusDays(30)));
        assertEquals("LOW_STOCK", statusCalculator.computeStatus(5, 10, today.plusDays(30)));

        // 4. In stock (above threshold and not expired)
        assertEquals("IN_STOCK", statusCalculator.computeStatus(25, 10, today.plusDays(30)));
    }

    @Test
    @DisplayName("Successful stock reservation increments reservedQuantity and is idempotent")
    void testStockReservationSuccessAndIdempotency() {
        PharmacyInventory inv = createSampleInventory(100L, 20, 5); // Available = 15

        when(inventoryRepository.findByPharmacyIdAndMedicineIdForUpdate(1L, 10L)).thenReturn(Optional.of(inv));
        when(transactionRepository.findByInventoryIdAndReferenceCodeAndTxnType(100L, "REQ-001", "RESERVE"))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any(PharmacyInventory.class))).thenAnswer(i -> i.getArgument(0));

        // Reserve 5 units
        Long resultId = inventoryService.reserve(1L, 10L, 5, "REQ-001");
        assertEquals(100L, resultId);
        assertEquals(10, inv.getReservedQuantity());
        assertEquals(10, inv.getAvailableQuantity());

        // Idempotency: second call with same reference code does not reserve again
        InventoryTransaction existingTx = new InventoryTransaction();
        when(transactionRepository.findByInventoryIdAndReferenceCodeAndTxnType(100L, "REQ-001", "RESERVE"))
                .thenReturn(Optional.of(existingTx));

        Long secondCall = inventoryService.reserve(1L, 10L, 5, "REQ-001");
        assertEquals(100L, secondCall);
        assertEquals(10, inv.getReservedQuantity()); // Unchanged
    }

    @Test
    @DisplayName("Insufficient stock reservation throws InsufficientStockException and does not change quantities")
    void testInsufficientStockReservationThrows() {
        PharmacyInventory inv = createSampleInventory(100L, 10, 8); // Available = 2

        when(inventoryRepository.findByPharmacyIdAndMedicineIdForUpdate(1L, 10L)).thenReturn(Optional.of(inv));
        when(transactionRepository.findByInventoryIdAndReferenceCodeAndTxnType(100L, "REQ-002", "RESERVE"))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientStockException.class, () -> {
            inventoryService.reserve(1L, 10L, 5, "REQ-002");
        });

        assertEquals(8, inv.getReservedQuantity()); // Still unchanged
    }

    @Test
    @DisplayName("Stock commit decreases quantity and reservedQuantity")
    void testStockCommit() {
        PharmacyInventory inv = createSampleInventory(100L, 20, 5);

        when(inventoryRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(inv));
        when(transactionRepository.findByInventoryIdAndReferenceCodeAndTxnType(100L, "REQ-003", "COMMIT"))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any(PharmacyInventory.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.commit(100L, 5, "REQ-003");

        assertEquals(15, inv.getQuantity());
        assertEquals(0, inv.getReservedQuantity());
    }

    @Test
    @DisplayName("Stock release decrements reservedQuantity without affecting total quantity")
    void testStockRelease() {
        PharmacyInventory inv = createSampleInventory(100L, 20, 5);

        when(inventoryRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(inv));
        when(transactionRepository.findByInventoryIdAndReferenceCodeAndTxnType(100L, "REQ-004", "RELEASE"))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any(PharmacyInventory.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.release(100L, 5, "REQ-004");

        assertEquals(20, inv.getQuantity());
        assertEquals(0, inv.getReservedQuantity());
    }
}
