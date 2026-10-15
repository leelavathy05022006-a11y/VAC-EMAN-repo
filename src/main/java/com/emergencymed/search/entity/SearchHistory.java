package com.emergencymed.search.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_history", indexes = {
    @Index(name = "idx_search_history_user_created", columnList = "user_id, created_at")
})
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "query_text", length = 120)
    private String queryText;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "user_lat", precision = 6, scale = 3)
    private BigDecimal userLat;

    @Column(name = "user_lng", precision = 6, scale = 3)
    private BigDecimal userLng;

    @Column(name = "radius_km", precision = 4, scale = 1)
    private BigDecimal radiusKm;

    @Column(name = "sort_by", length = 15)
    private String sortBy;

    @Column(name = "results_count")
    private Integer resultsCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public SearchHistory() {
    }

    public SearchHistory(Long userId, Long medicineId, String queryText, Integer quantity, BigDecimal userLat, BigDecimal userLng, BigDecimal radiusKm, String sortBy, Integer resultsCount, LocalDateTime createdAt) {
        this.userId = userId;
        this.medicineId = medicineId;
        this.queryText = queryText;
        this.quantity = quantity;
        this.userLat = userLat;
        this.userLng = userLng;
        this.radiusKm = radiusKm;
        this.sortBy = sortBy;
        this.resultsCount = resultsCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUserLat() {
        return userLat;
    }

    public void setUserLat(BigDecimal userLat) {
        this.userLat = userLat;
    }

    public BigDecimal getUserLng() {
        return userLng;
    }

    public void setUserLng(BigDecimal userLng) {
        this.userLng = userLng;
    }

    public BigDecimal getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(BigDecimal radiusKm) {
        this.radiusKm = radiusKm;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Integer getResultsCount() {
        return resultsCount;
    }

    public void setResultsCount(Integer resultsCount) {
        this.resultsCount = resultsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
