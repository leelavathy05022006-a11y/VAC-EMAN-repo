package com.emergencymed.common.contract;

import com.emergencymed.common.dto.AlternativesDto;
import com.emergencymed.common.dto.MedicineOfferDto;
import java.util.List;

public interface InventoryQueryService {
    List<MedicineOfferDto> findOffers(Long medicineId, int qty, List<Long> pharmacyIds);
    AlternativesDto findAlternatives(Long medicineId, List<Long> pharmacyIds);
}
