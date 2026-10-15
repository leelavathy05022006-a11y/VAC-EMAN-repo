package com.emergencymed.auth.repository;

import com.emergencymed.auth.entity.Pharmacy;
import com.emergencymed.auth.entity.User;
import com.emergencymed.common.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByUserId(Long userId);
    Optional<Pharmacy> findByUser(User user);
    boolean existsByLicenseNo(String licenseNo);
    long countByApprovalStatus(ApprovalStatus status);

    List<Pharmacy> findByLatitudeBetweenAndLongitudeBetweenAndApprovalStatusAndIsActiveTrue(
            Double minLat, Double maxLat, Double minLon, Double maxLon, ApprovalStatus status
    );

    @Query("SELECT p FROM Pharmacy p WHERE " +
           "(:status IS NULL OR p.approvalStatus = :status) AND " +
           "(:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.licenseNo) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.city) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Pharmacy> searchPharmacies(@Param("status") ApprovalStatus status,
                                    @Param("q") String q,
                                    Pageable pageable);

    List<Pharmacy> findByCityIgnoreCaseAndIsActiveTrue(String city);

    List<Pharmacy> findByCityIgnoreCaseAndLocalityIgnoreCaseAndIsActiveTrue(String city, String locality);

    @Query("SELECT p FROM Pharmacy p WHERE " +
           "(:city IS NULL OR LOWER(p.city) = LOWER(:city)) AND " +
           "(:locality IS NULL OR LOWER(p.locality) = LOWER(:locality)) AND " +
           "(p.isActive = true)")
    List<Pharmacy> searchPharmaciesByCityAndLocality(@Param("city") String city, @Param("locality") String locality);

    @Query("SELECT p FROM Pharmacy p WHERE " +
           "p.isActive = true AND " +
           "(:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.locality) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.address) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Pharmacy> searchPharmacies(@Param("q") String q, Pageable pageable);

    @Query("SELECT p FROM Pharmacy p WHERE p.isActive = true AND (" +
           ":q IS NULL OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.locality) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.address) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "p.postalCode = :q OR p.pincode = :q)")
    List<Pharmacy> searchAllMatching(@Param("q") String q);

    @Query("SELECT DISTINCT p.locality FROM Pharmacy p WHERE LOWER(p.city) = LOWER(:city) AND p.isActive = true AND p.locality IS NOT NULL ORDER BY p.locality ASC")
    List<String> findDistinctLocalitiesByCity(@Param("city") String city);

    List<Pharmacy> findByIsApprovedFalseAndIsActiveTrueOrderByCreatedAtDesc();

    List<Pharmacy> findByPostalCodeAndIsActiveTrue(String postalCode);

    Optional<Pharmacy> findByProviderPlaceId(String providerPlaceId);
}
