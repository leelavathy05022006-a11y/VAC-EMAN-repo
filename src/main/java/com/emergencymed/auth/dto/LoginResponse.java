package com.emergencymed.auth.dto;

import com.emergencymed.common.enums.ApprovalStatus;
import com.emergencymed.common.enums.Role;

public record LoginResponse(
    Long userId,
    String fullName,
    String email,
    Role role,
    ApprovalStatus approvalStatus,
    String redirectUrl
) {}
