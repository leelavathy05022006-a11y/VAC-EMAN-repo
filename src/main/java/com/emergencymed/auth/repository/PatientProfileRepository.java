package com.emergencymed.auth.repository;

import com.emergencymed.auth.entity.PatientProfile;
import com.emergencymed.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    Optional<PatientProfile> findByUserId(Long userId);
    Optional<PatientProfile> findByUser(User user);
}
