package com.emergencymed.search.repository;

import com.emergencymed.search.entity.ContactLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactLogRepository extends JpaRepository<ContactLog, Long> {

    List<ContactLog> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);

    List<ContactLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
