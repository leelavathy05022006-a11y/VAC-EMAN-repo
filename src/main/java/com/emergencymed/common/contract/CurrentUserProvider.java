package com.emergencymed.common.contract;

import com.emergencymed.common.dto.*;

import com.emergencymed.common.enums.Role;
import java.util.Optional;

public interface CurrentUserProvider {
    Long userId();
    Role role();
    Optional<Long> pharmacyId();
    boolean isAuthenticated();
}
