package com.emergencymed.common.events;

public record PharmacyApprovedEvent(
    Long pharmacyId,
    Long userId,
    String pharmacyName,
    String licenseNo,
    Double latitude,
    Double longitude
) {
    public PharmacyApprovedEvent(Long pharmacyId, Long userId) {
        this(pharmacyId, userId, null, null, null, null);
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public Long getUserId() {
        return userId;
    }
}
