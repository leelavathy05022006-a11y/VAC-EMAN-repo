package com.emergencymed.auth.security;

import com.emergencymed.auth.repository.PharmacyRepository;
import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.enums.Role;
import com.emergencymed.common.exception.BusinessException;
import com.emergencymed.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserProviderImpl implements CurrentUserProvider {

    private final PharmacyRepository pharmacyRepository;

    public CurrentUserProviderImpl(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private CustomUserDetails getPrincipal() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails principal) {
            return principal;
        }
        return null;
    }

    @Override
    public Long userId() {
        CustomUserDetails principal = getPrincipal();
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User is not authenticated");
        }
        return principal.getUserId();
    }

    @Override
    public Role role() {
        CustomUserDetails principal = getPrincipal();
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User is not authenticated");
        }
        return principal.getRole();
    }

    @Override
    public Optional<Long> pharmacyId() {
        CustomUserDetails principal = getPrincipal();
        if (principal == null) {
            return Optional.empty();
        }
        if (principal.getPharmacyId() != null) {
            return Optional.of(principal.getPharmacyId());
        }
        if (principal.getRole() == Role.PHARMACY) {
            return pharmacyRepository.findByUserId(principal.getUserId()).map(p -> p.getId());
        }
        return Optional.empty();
    }

    @Override
    public boolean isAuthenticated() {
        CustomUserDetails principal = getPrincipal();
        return principal != null;
    }
}
