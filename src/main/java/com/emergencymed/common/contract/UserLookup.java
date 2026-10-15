package com.emergencymed.common.contract;

import com.emergencymed.common.dto.*;

import java.util.List;
import java.util.Optional;

public interface UserLookup {
    Optional<UserDto> findById(Long userId);
    List<Long> activeDeliveryPartnerIds();
}
