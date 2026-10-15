package com.emergencymed.inventory.service.impl;

import com.emergencymed.common.contract.InventoryQueryService;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.dto.AlternativesDto;
import com.emergencymed.common.dto.MedicineDto;
import com.emergencymed.common.dto.MedicineOfferDto;
import com.emergencymed.common.dto.StockStatus;
import com.emergencymed.inventory.entity.Medicine;
import com.emergencymed.inventory.entity.PharmacyInventory;
import com.emergencymed.inventory.repository.MedicineRepository;
import com.emergencymed.inventory.repository.PharmacyInventoryRepository;
import com.emergencymed.inventory.service.StockStatusCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InventoryQueryServiceImpl implements InventoryQueryService {
    private final PharmacyInventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;
    private final PharmacyLookup pharmacyLookup;
    private final StockStatusCalculator statusCalculator;

    public InventoryQueryServiceImpl(PharmacyInventoryRepository inventoryRepository,
                                     MedicineRepository medicineRepository,
                                     PharmacyLookup pharmacyLookup,
                                     StockStatusCalculator statusCalculator) {
        this.inventoryRepository = inventoryRepository;
        this.medicineRepository = medicineRepository;
        this.pharmacyLookup = pharmacyLookup;
        this.statusCalculator = statusCalculator;
    }

    @Override
    public List<MedicineOfferDto> findOffers(Long medicineId, int qty, List<Long> pharmacyIds) {
        if (pharmacyIds == null || pharmacyIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> approvedPharmacies = pharmacyIds.stream()
                .filter(pharmacyLookup::isApproved)
                .collect(Collectors.toList());

        if (approvedPharmacies.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        List<PharmacyInventory> list = inventoryRepository.findAvailableOffers(medicineId, qty > 0 ? qty : 1, approvedPharmacies, today);

        return list.stream()
                .map(this::toMedicineOfferDto)
                .collect(Collectors.toList());
    }

    @Override
    public AlternativesDto findAlternatives(Long medicineId, List<Long> pharmacyIds) {
        Medicine target = medicineRepository.findById(medicineId).orElse(null);
        if (target == null) {
            return new AlternativesDto();
        }

        List<Medicine> substitutes = medicineRepository.findByGenericNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCase(
                target.getGenericName(), target.getStrength(), target.getDosageForm()
        );

        List<MedicineDto> genericSubstitutes = substitutes.stream()
                .filter(m -> !m.getId().equals(medicineId))
                .map(m -> new MedicineDto(
                        m.getId(),
                        m.getName(),
                        m.getGenericName(),
                        m.getDosageForm(),
                        m.getStrength(),
                        m.getManufacturer(),
                        Boolean.TRUE.equals(m.getRequiresPrescription()),
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        List<MedicineOfferDto> partialOffers = new ArrayList<>();
        if (pharmacyIds != null && !pharmacyIds.isEmpty()) {
            List<Long> approvedPharmacies = pharmacyIds.stream()
                    .filter(pharmacyLookup::isApproved)
                    .collect(Collectors.toList());
            if (!approvedPharmacies.isEmpty()) {
                List<PharmacyInventory> partials = inventoryRepository.findAvailableOffers(medicineId, 1, approvedPharmacies, LocalDate.now());
                partialOffers = partials.stream().map(this::toMedicineOfferDto).collect(Collectors.toList());
            }
        }

        return new AlternativesDto(genericSubstitutes, partialOffers);
    }

    private MedicineOfferDto toMedicineOfferDto(PharmacyInventory inv) {
        String statusStr = statusCalculator.computeStatus(inv.getAvailableQuantity(), inv.getLowStockThreshold(), inv.getExpiryDate());
        StockStatus stockStatus;
        try {
            stockStatus = StockStatus.valueOf(statusStr);
        } catch (Exception e) {
            stockStatus = StockStatus.IN_STOCK;
        }

        return new MedicineOfferDto(
                inv.getId(),
                inv.getPharmacyId(),
                inv.getMedicine().getId(),
                inv.getAvailableQuantity(),
                inv.getReservedQuantity(),
                inv.getUnitPrice(),
                stockStatus,
                inv.getDataSource(),
                inv.getLastUpdatedAt() != null ? inv.getLastUpdatedAt().toString() : "",
                inv.getExpiryDate(),
                inv.getBatchNo()
        );
    }
}
