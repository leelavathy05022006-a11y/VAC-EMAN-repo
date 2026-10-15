package com.emergencymed.inventory.repository;

import com.emergencymed.inventory.entity.MedicineCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineCategoryRepository extends JpaRepository<MedicineCategory, Long> {
    Optional<MedicineCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<MedicineCategory> findByIsActiveTrue();
}
