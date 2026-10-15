package com.emergencymed.auth.controller;

import com.emergencymed.auth.dto.UpdateLocationRequest;
import com.emergencymed.auth.dto.UpdateProfileRequest;
import com.emergencymed.auth.dto.UserProfileResponse;
import com.emergencymed.auth.service.ProfileService;
import com.emergencymed.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final ProfileService profileService;

    public UserController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        UserProfileResponse profile = profileService.getUserProfile();
        return ResponseEntity.ok(ApiResponse.success(profile, "User profile retrieved"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        UserProfileResponse updated = profileService.updateProfile(req);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @PutMapping("/me/location")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateLocation(@Valid @RequestBody UpdateLocationRequest req) {
        UserProfileResponse updated = profileService.updateLocation(req);
        return ResponseEntity.ok(ApiResponse.success(updated, "Location updated successfully"));
    }
}
