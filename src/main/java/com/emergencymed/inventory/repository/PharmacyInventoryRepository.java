package com.emergencymed.inventory.repository;

import com.emergencymed.inventory.entity.PharmacyInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyInventoryRepository extends JpaRepository<PharmacyInventory, Long> {

    Optional<PharmacyInventory> findByPharmacyIdAndId(Long pharmacyId, Long id);

    Optional<PharmacyInventory> findByPharmacyIdAndMedicineId(Long pharmacyId, Long medicineId);

    boolean existsByPharmacyIdAndMedicineId(Long pharmacyId, Long medicineId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM PharmacyInventory i WHERE i.id = :id")
    Optional<PharmacyInventory> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM PharmacyInventory i WHERE i.pharmacyId = :pharmacyId AND i.medicine.id = :medicineId")
    Optional<PharmacyInventory> findByPharmacyIdAndMedicineIdForUpdate(@Param("pharmacyId") Long pharmacyId, @Param("medicineId") Long medicineId);

    List<PharmacyInventory> findByPharmacyId(Long pharmacyId);

    List<PharmacyInventory> findByPharmacyIdAndIsActiveTrue(Long pharmacyId);

    @Query("SELECT i FROM PharmacyInventory i WHERE " +
           "i.pharmacyId = :pharmacyId AND " +
           "(:q IS NULL OR LOWER(i.medicine.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(i.medicine.genericName) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(i.isActive = true)")
    Page<PharmacyInventory> findByPharmacyIdAndQuery(@Param("pharmacyId") Long pharmacyId,
                                                      @Param("q") String q,
                                                      Pageable pageable);

    @Query("SELECT i FROM PharmacyInventory i WHERE " +
           "i.medicine.id = :medicineId AND " +
           "i.pharmacyId IN :pharmacyIds AND " +
           "i.isActive = true AND " +
           "i.expiryDate > :today AND " +
           "(i.quantity - i.reservedQuantity) >= :qty")
    List<PharmacyInventory> findAvailableOffers(@Param("medicineId") Long medicineId,
                                                @Param("qty") Integer qty,
                                                @Param("pharmacyIds") List<Long> pharmacyIds,
                                                @Param("today") LocalDate today);

    @Query("SELECT i FROM PharmacyInventory i WHERE " +
           "i.medicine.id IN :medicineIds AND " +
           "i.pharmacyId IN :pharmacyIds AND " +
           "i.isActive = true AND " +
           "i.expiryDate > :today AND " +
           "(i.quantity - i.reservedQuantity) > 0")
    List<PharmacyInventory> findAlternativeOffers(@Param("medicineIds") List<Long> medicineIds,
                                                   @Param("pharmacyIds") List<Long> pharmacyIds,
                                                   @Param("today") LocalDate today);
}
