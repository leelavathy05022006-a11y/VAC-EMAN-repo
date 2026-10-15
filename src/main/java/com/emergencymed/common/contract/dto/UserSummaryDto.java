package com.emergencymed.common.contract.dto;

import com.emergencymed.common.enums.Role;

public record UserSummaryDto(
    Long id,
    String fullName,
    String email,
    String phone,
    Role role,
    Boolean isActive
) {}
