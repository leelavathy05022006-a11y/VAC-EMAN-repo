package com.emergencymed.auth.repository;

import com.emergencymed.auth.entity.User;
import com.emergencymed.common.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);

    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:active IS NULL OR u.isActive = :active) AND " +
           "(:q IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<User> searchUsers(@Param("role") Role role,
                           @Param("active") Boolean active,
                           @Param("q") String q,
                           Pageable pageable);
}
