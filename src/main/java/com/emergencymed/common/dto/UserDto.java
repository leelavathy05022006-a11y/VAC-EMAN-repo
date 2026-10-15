package com.emergencymed.common.dto;

import com.emergencymed.common.enums.Role;

public class UserDto {
    private Long id;
    private String email;
    private Role role;
    private String fullName;
    private String phone;
    private String status = "ACTIVE";
    private boolean active = true;

    public UserDto() {
    }

    public UserDto(Long id, String email, Role role, String fullName, String phone, String status) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.status = status;
        this.active = !"INACTIVE".equalsIgnoreCase(status);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.active = !"INACTIVE".equalsIgnoreCase(status);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.status = active ? "ACTIVE" : "INACTIVE";
    }
}
