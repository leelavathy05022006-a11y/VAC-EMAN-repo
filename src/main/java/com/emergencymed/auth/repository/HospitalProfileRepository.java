package com.emergencymed.auth.repository;

import com.emergencymed.auth.entity.HospitalProfile;
import com.emergencymed.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalProfileRepository extends JpaRepository<HospitalProfile, Long> {
    Optional<HospitalProfile> findByUserId(Long userId);
    Optional<HospitalProfile> findByUser(User user);
    boolean existsByRegistrationNo(String registrationNo);
}
