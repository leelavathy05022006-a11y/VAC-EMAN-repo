package com.emergencymed.inventory.repository;

import com.emergencymed.inventory.entity.InventoryUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryUpdateLogRepository extends JpaRepository<InventoryUpdateLog, Long> {
    List<InventoryUpdateLog> findByPharmacyIdOrderByUpdatedAtDesc(Long pharmacyId);
    List<InventoryUpdateLog> findByInventoryIdOrderByUpdatedAtDesc(Long inventoryId);
}
