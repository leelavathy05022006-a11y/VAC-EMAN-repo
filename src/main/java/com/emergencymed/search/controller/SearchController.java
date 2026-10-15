package com.emergencymed.search.controller;

import com.emergencymed.common.dto.ApiResponse;
import com.emergencymed.common.dto.PageResponse;
import com.emergencymed.search.dto.*;
import com.emergencymed.search.entity.SearchHistory;
import com.emergencymed.search.service.AlternativeSuggestionService;
import com.emergencymed.search.service.ComparisonService;
import com.emergencymed.search.service.PharmacySearchService;
import com.emergencymed.search.service.SearchHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final PharmacySearchService pharmacySearchService;
    private final AlternativeSuggestionService alternativeSuggestionService;
    private final ComparisonService comparisonService;
    private final SearchHistoryService searchHistoryService;

    @Autowired
    public SearchController(PharmacySearchService pharmacySearchService,
                            AlternativeSuggestionService alternativeSuggestionService,
                            ComparisonService comparisonService,
                            SearchHistoryService searchHistoryService) {
        this.pharmacySearchService = pharmacySearchService;
        this.alternativeSuggestionService = alternativeSuggestionService;
        this.comparisonService = comparisonService;
        this.searchHistoryService = searchHistoryService;
    }

    /**
     * GET /api/v1/search/pharmacies
     * Performs medicine availability search. Returns matching pharmacies or alternatives on zero matches.
     */
    @GetMapping("/pharmacies")
    public ApiResponse<Object> searchPharmacies(
            @RequestParam("medicineId") Long medicineId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng,
            @RequestParam(value = "radiusKm", defaultValue = "10.0") Double radiusKm,
            @RequestParam(value = "sort", defaultValue = "distance") String sort,
            @RequestParam(value = "deliveryOnly", defaultValue = "false") Boolean deliveryOnly,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {

        SearchRequest request = new SearchRequest();
        request.setMedicineId(medicineId);
        request.setQuantity(quantity);
        request.setLat(lat);
        request.setLng(lng);
        request.setRadiusKm(radiusKm);
        request.setSort(sort);
        request.setDeliveryOnly(deliveryOnly);
        request.setPage(page);
        request.setSize(size);

        List<PharmacyMatch> allMatches = pharmacySearchService.searchPharmacies(request);

        // If zero matches, return HTTP 200 with alternatives block per specification
        if (allMatches.isEmpty()) {
            AlternativesResponse alternatives = alternativeSuggestionService.findAlternatives(
                    medicineId, quantity, lat, lng, radiusKm
            );
            return ApiResponse.ok("Zero direct matches found. Showing recommended alternatives.", alternatives);
        }

        // Apply pagination
        int pageIndex = Math.max(0, page);
        int pageSize = Math.min(Math.max(1, size), 50); // size max 50 per spec
        int totalElements = allMatches.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        int fromIndex = pageIndex * pageSize;
        List<PharmacyMatch> pagedContent;
        if (fromIndex >= totalElements) {
            pagedContent = List.of();
        } else {
            int toIndex = Math.min(fromIndex + pageSize, totalElements);
            pagedContent = allMatches.subList(fromIndex, toIndex);
        }

        PageResponse<PharmacyMatch> pageResponse = new PageResponse<>(
                pagedContent, pageIndex, pageSize, totalElements, totalPages
        );

        return ApiResponse.ok("Found " + totalElements + " matching pharmacies.", pageResponse);
    }

    /**
     * GET /api/v1/search/compare
     * Compares 2 to 3 distinct approved pharmacies.
     */
    @GetMapping("/compare")
    public ApiResponse<CompareResponse> comparePharmacies(
            @RequestParam("pharmacyIds") List<Long> pharmacyIds,
            @RequestParam("medicineId") Long medicineId,
            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng) {

        CompareResponse response = comparisonService.comparePharmacies(pharmacyIds, medicineId, quantity, lat, lng);
        return ApiResponse.ok(response);
    }

    /**
     * GET /api/v1/search/history
     * Returns the last 10 searches of the authenticated user.
     */
    @GetMapping("/history")
    public ApiResponse<List<SearchHistory>> getSearchHistory() {
        List<SearchHistory> history = searchHistoryService.getLast10Searches();
        return ApiResponse.ok("Recent searches retrieved successfully", history);
    }

    /**
     * DELETE /api/v1/search/history
     * Clears all search history for the authenticated user.
     */
    @DeleteMapping("/history")
    public ApiResponse<Object> clearSearchHistory() {
        long deletedCount = searchHistoryService.clearHistory();
        return ApiResponse.ok("Cleared " + deletedCount + " search history records", null);
    }
}
