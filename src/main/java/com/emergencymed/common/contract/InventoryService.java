package com.emergencymed.common.contract;

import com.emergencymed.common.dto.*;

public interface InventoryService {
    Long reserve(Long pharmacyId, Long medicineId, int qty, String refCode);
    void commit(Long inventoryId, int qty, String refCode);
    void release(Long inventoryId, int qty, String refCode);
    void restore(Long inventoryId, int qty, String refCode);
}
