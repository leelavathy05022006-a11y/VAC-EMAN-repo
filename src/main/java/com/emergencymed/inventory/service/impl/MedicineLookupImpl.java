package com.emergencymed.inventory.service.impl;

import com.emergencymed.common.dto.MedicineDto;

import com.emergencymed.common.contract.MedicineLookup;
import com.emergencymed.inventory.entity.Medicine;
import com.emergencymed.inventory.repository.MedicineRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MedicineLookupImpl implements MedicineLookup {
    private final MedicineRepository medicineRepository;

    public MedicineLookupImpl(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public Optional<MedicineDto> findById(Long id) {
        return medicineRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<MedicineDto> searchByName(String q, int limit) {
        int max = limit > 0 ? limit : 10;
        return medicineRepository.searchByName(q, PageRequest.of(0, max)).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private MedicineDto toDto(Medicine m) {
        return new MedicineDto(
                m.getId(),
                m.getName(),
                m.getGenericName(),
                m.getDosageForm(),
                m.getStrength(),
                m.getManufacturer(),
                Boolean.TRUE.equals(m.getRequiresPrescription()),
                BigDecimal.ZERO
        );
    }
}
