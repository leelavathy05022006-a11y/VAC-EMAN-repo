package com.emergencymed.inventory.repository;

import com.emergencymed.inventory.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Page<InventoryTransaction> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId, Pageable pageable);

    Optional<InventoryTransaction> findByInventoryIdAndReferenceCodeAndTxnType(Long inventoryId, String referenceCode, String txnType);

    List<InventoryTransaction> findByInventoryIdOrderByCreatedAtAsc(Long inventoryId);
}
