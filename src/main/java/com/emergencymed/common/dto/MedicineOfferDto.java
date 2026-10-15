package com.emergencymed.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicineOfferDto {
    private Long inventoryId;
    private Long pharmacyId;
    private Long medicineId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private BigDecimal unitPrice;
    private StockStatus stockStatus;
    private String dataSource; // "SAMPLE" or "LIVE"
    private String lastUpdatedAt;
    private LocalDate expiryDate;
    private String batchNumber;

    public MedicineOfferDto() {
    }

    public MedicineOfferDto(Long inventoryId, Long pharmacyId, Long medicineId, Integer availableQuantity, Integer reservedQuantity, BigDecimal unitPrice, StockStatus stockStatus, String dataSource, String lastUpdatedAt, LocalDate expiryDate, String batchNumber) {
        this.inventoryId = inventoryId;
        this.pharmacyId = pharmacyId;
        this.medicineId = medicineId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.unitPrice = unitPrice;
        this.stockStatus = stockStatus;
        this.dataSource = dataSource;
        this.lastUpdatedAt = lastUpdatedAt;
        this.expiryDate = expiryDate;
        this.batchNumber = batchNumber;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public StockStatus getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(StockStatus stockStatus) {
        this.stockStatus = stockStatus;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
}
