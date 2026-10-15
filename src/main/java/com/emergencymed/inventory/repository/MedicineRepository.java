package com.emergencymed.inventory.repository;

import com.emergencymed.inventory.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    boolean existsByNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCase(String name, String strength, String dosageForm);
    boolean existsByNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndIdNot(String name, String strength, String dosageForm, Long id);
    Optional<Medicine> findByNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCase(String name, String strength, String dosageForm);

    @Query("SELECT m FROM Medicine m WHERE " +
           "(:q IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
           "(:requiresPrescription IS NULL OR m.requiresPrescription = :requiresPrescription) AND " +
           "(m.isActive = true)")
    Page<Medicine> searchMedicines(@Param("q") String q,
                                   @Param("categoryId") Long categoryId,
                                   @Param("requiresPrescription") Boolean requiresPrescription,
                                   Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Medicine> searchByName(@Param("q") String q, Pageable pageable);

    List<Medicine> findByGenericNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCase(String genericName, String strength, String dosageForm);
}
