package com.emergencymed.inventory.service;

import com.emergencymed.inventory.dto.CategoryDto;
import com.emergencymed.inventory.entity.MedicineCategory;
import com.emergencymed.inventory.exception.DuplicateResourceException;
import com.emergencymed.inventory.exception.ResourceNotFoundException;
import com.emergencymed.inventory.repository.MedicineCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    private final MedicineCategoryRepository categoryRepository;

    public CategoryService(MedicineCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        MedicineCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return toDto(cat);
    }

    public CategoryDto createCategory(CategoryDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new DuplicateResourceException("Category with name '" + dto.name() + "' already exists");
        }
        MedicineCategory cat = new MedicineCategory(dto.name(), dto.description(), dto.isActive());
        MedicineCategory saved = categoryRepository.save(cat);
        return toDto(saved);
    }

    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        MedicineCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.name(), id)) {
            throw new DuplicateResourceException("Category with name '" + dto.name() + "' already exists");
        }
        cat.setName(dto.name());
        cat.setDescription(dto.description());
        if (dto.isActive() != null) {
            cat.setIsActive(dto.isActive());
        }
        MedicineCategory saved = categoryRepository.save(cat);
        return toDto(saved);
    }

    private CategoryDto toDto(MedicineCategory cat) {
        return new CategoryDto(cat.getId(), cat.getName(), cat.getDescription(), cat.getIsActive());
    }
}
