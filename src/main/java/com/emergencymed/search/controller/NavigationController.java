package com.emergencymed.search.controller;

import com.emergencymed.common.dto.ApiResponse;
import com.emergencymed.search.service.NavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/navigation")
public class NavigationController {

    private final NavigationService navigationService;

    @Autowired
    public NavigationController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    /**
     * GET /api/v1/navigation/{pharmacyId}
     * Returns Google Maps directions URL and automatically logs a NAVIGATION action.
     */
    @GetMapping("/{pharmacyId}")
    public ApiResponse<Map<String, String>> getNavigation(
            @PathVariable("pharmacyId") Long pharmacyId,
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng,
            @RequestParam(value = "mode", defaultValue = "driving") String mode) {

        Map<String, String> directions = navigationService.getDirections(pharmacyId, lat, lng, mode);
        return ApiResponse.ok("Navigation directions created and logged", directions);
    }
}
