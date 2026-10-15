package com.emergencymed.common.contract;

import com.emergencymed.common.dto.*;

import com.emergencymed.common.enums.DeliveryStatus;

public interface DeliveryAvailabilityQuery {
    DeliveryStatus statusFor(Long pharmacyId);
}
