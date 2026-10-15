package com.emergencymed.inventory.controller;

import com.emergencymed.inventory.dto.ApiResponse;
import com.emergencymed.inventory.dto.PharmacySettingsDto;
import com.emergencymed.inventory.service.PharmacySettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pharmacy-settings")
public class PharmacySettingsController {
    private final PharmacySettingsService settingsService;

    public PharmacySettingsController(PharmacySettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/me")
    public ApiResponse<PharmacySettingsDto> getCurrentSettings() {
        return ApiResponse.ok(settingsService.getCurrentPharmacySettings());
    }

    @GetMapping("/{pharmacyId}")
    public ApiResponse<PharmacySettingsDto> getSettingsByPharmacyId(@PathVariable Long pharmacyId) {
        return ApiResponse.ok(settingsService.getSettingsByPharmacyId(pharmacyId));
    }

    @PutMapping("/me")
    public ApiResponse<PharmacySettingsDto> updateCurrentSettings(@Valid @RequestBody PharmacySettingsDto dto) {
        return ApiResponse.ok(settingsService.updateCurrentPharmacySettings(dto), "Pharmacy settings updated successfully");
    }
}
