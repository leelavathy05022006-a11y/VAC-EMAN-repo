package com.emergencymed.auth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePharmacyRequest(
    @NotBlank(message = "Pharmacy name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    String name,

    @Size(max = 20, message = "GST number must not exceed 20 characters")
    String gstNo,

    @NotBlank(message = "Address is required")
    String address,

    String city,
    String pincode,

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    Double latitude,

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    Double longitude
) {}
