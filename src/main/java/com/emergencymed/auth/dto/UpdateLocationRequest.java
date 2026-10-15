package com.emergencymed.auth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

public record UpdateLocationRequest(
    String address,
    String city,

    @Pattern(regexp = "^$|^[0-9]{6}$", message = "Pincode must be 6 digits")
    String pincode,

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    Double latitude,

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    Double longitude
) {}
