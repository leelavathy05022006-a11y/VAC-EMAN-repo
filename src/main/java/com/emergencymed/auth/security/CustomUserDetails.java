package com.emergencymed.auth.security;

import com.emergencymed.auth.entity.User;
import com.emergencymed.common.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final Role role;
    private final boolean active;
    private final Long pharmacyId;

    public CustomUserDetails(User user, Long pharmacyId) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.active = user.isActive();
        this.pharmacyId = pharmacyId;
    }

    public CustomUserDetails(Long userId, String email, Role role, Long pharmacyId) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = "";
        this.fullName = "Test User";
        this.role = role;
        this.active = true;
        this.pharmacyId = pharmacyId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
