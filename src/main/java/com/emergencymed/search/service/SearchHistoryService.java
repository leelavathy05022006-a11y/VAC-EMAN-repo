package com.emergencymed.search.service;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.exception.UnauthorizedException;
import com.emergencymed.search.entity.SearchHistory;
import com.emergencymed.search.repository.SearchHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchHistoryService {

    private final SearchHistoryRepository repository;
    private final CurrentUserProvider currentUserProvider;

    @Autowired
    public SearchHistoryService(SearchHistoryRepository repository, CurrentUserProvider currentUserProvider) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Saves a search history record with coordinates rounded to 3 decimal places for privacy.
     */
    @Transactional
    public SearchHistory logSearch(Long medicineId, String queryText, Integer quantity,
                                  Double lat, Double lng, Double radiusKm, String sortBy, int resultsCount) {
        if (!currentUserProvider.isAuthenticated()) {
            return null; // Do not record for unauthenticated guest
        }

        Long userId = currentUserProvider.userId();
        if (userId == null) {
            return null;
        }

        BigDecimal roundedLat = lat != null ? BigDecimal.valueOf(lat).setScale(3, RoundingMode.HALF_UP) : null;
        BigDecimal roundedLng = lng != null ? BigDecimal.valueOf(lng).setScale(3, RoundingMode.HALF_UP) : null;
        BigDecimal roundedRadius = radiusKm != null ? BigDecimal.valueOf(radiusKm).setScale(1, RoundingMode.HALF_UP) : null;

        SearchHistory history = new SearchHistory(
                userId,
                medicineId,
                queryText,
                quantity,
                roundedLat,
                roundedLng,
                roundedRadius,
                sortBy,
                resultsCount,
                LocalDateTime.now()
        );

        return repository.save(history);
    }

    /**
     * Fetches the last 10 searches for the current authenticated user.
     */
    @Transactional(readOnly = true)
    public List<SearchHistory> getLast10Searches() {
        if (!currentUserProvider.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        Long userId = currentUserProvider.userId();
        return repository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Clears all search history for the current authenticated user.
     */
    @Transactional
    public long clearHistory() {
        if (!currentUserProvider.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        Long userId = currentUserProvider.userId();
        return repository.deleteByUserId(userId);
    }
}
