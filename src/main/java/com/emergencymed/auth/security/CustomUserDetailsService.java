package com.emergencymed.auth.security;

import com.emergencymed.auth.entity.Pharmacy;
import com.emergencymed.auth.entity.User;
import com.emergencymed.auth.repository.PharmacyRepository;
import com.emergencymed.auth.repository.UserRepository;
import com.emergencymed.common.enums.Role;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;

    public CustomUserDetailsService(UserRepository userRepository, PharmacyRepository pharmacyRepository) {
        this.userRepository = userRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.isActive()) {
            throw new DisabledException("Account has been deactivated. Please contact administrator.");
        }

        Long pharmacyId = null;
        if (user.getRole() == Role.PHARMACY) {
            pharmacyId = pharmacyRepository.findByUserId(user.getId())
                    .map(Pharmacy::getId)
                    .orElse(null);
        }

        return new CustomUserDetails(user, pharmacyId);
    }
}
