package com.emergencymed.inventory.service;

import com.emergencymed.inventory.dto.MedicineDto;
import com.emergencymed.inventory.entity.Medicine;
import com.emergencymed.inventory.entity.MedicineCategory;
import com.emergencymed.inventory.exception.DuplicateResourceException;
import com.emergencymed.inventory.exception.ResourceNotFoundException;
import com.emergencymed.inventory.repository.MedicineCategoryRepository;
import com.emergencymed.inventory.repository.MedicineRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MedicineService {
    private final MedicineRepository medicineRepository;
    private final MedicineCategoryRepository categoryRepository;

    public MedicineService(MedicineRepository medicineRepository, MedicineCategoryRepository categoryRepository) {
        this.medicineRepository = medicineRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<MedicineDto> searchMedicines(String q, Long categoryId, Boolean requiresPrescription, Pageable pageable) {
        return medicineRepository.searchMedicines(q, categoryId, requiresPrescription, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public MedicineDto getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + id));
        return toDto(medicine);
    }

    public MedicineDto createMedicine(MedicineDto dto) {
        if (medicineRepository.existsByNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCase(
                dto.name(), dto.strength(), dto.dosageForm())) {
            throw new DuplicateResourceException("Medicine with name '" + dto.name() + "', strength '" +
                    dto.strength() + "' and dosage form '" + dto.dosageForm() + "' already exists");
        }
        MedicineCategory category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.categoryId()));

        Medicine medicine = new Medicine(
                category,
                dto.name(),
                dto.genericName(),
                dto.strength(),
                dto.dosageForm(),
                dto.manufacturer(),
                dto.requiresPrescription(),
                dto.isActive()
        );
        Medicine saved = medicineRepository.save(medicine);
        return toDto(saved);
    }

    public MedicineDto updateMedicine(Long id, MedicineDto dto) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + id));

        if (medicineRepository.existsByNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndIdNot(
                dto.name(), dto.strength(), dto.dosageForm(), id)) {
            throw new DuplicateResourceException("Medicine with name '" + dto.name() + "', strength '" +
                    dto.strength() + "' and dosage form '" + dto.dosageForm() + "' already exists");
        }

        MedicineCategory category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.categoryId()));

        medicine.setCategory(category);
        medicine.setName(dto.name());
        medicine.setGenericName(dto.genericName());
        medicine.setStrength(dto.strength());
        medicine.setDosageForm(dto.dosageForm());
        medicine.setManufacturer(dto.manufacturer());
        if (dto.requiresPrescription() != null) {
            medicine.setRequiresPrescription(dto.requiresPrescription());
        }
        if (dto.isActive() != null) {
            medicine.setIsActive(dto.isActive());
        }
        Medicine saved = medicineRepository.save(medicine);
        return toDto(saved);
    }

    public MedicineDto updateStatus(Long id, Boolean active) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + id));
        medicine.setIsActive(active);
        Medicine saved = medicineRepository.save(medicine);
        return toDto(saved);
    }

    public MedicineDto toDto(Medicine m) {
        return new MedicineDto(
                m.getId(),
                m.getCategory().getId(),
                m.getCategory().getName(),
                m.getName(),
                m.getGenericName(),
                m.getStrength(),
                m.getDosageForm(),
                m.getManufacturer(),
                m.getRequiresPrescription(),
                m.getIsActive()
        );
    }
}
