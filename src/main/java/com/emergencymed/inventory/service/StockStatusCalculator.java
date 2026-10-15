package com.emergencymed.inventory.service;

import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class StockStatusCalculator {
    public static final String EXPIRED = "EXPIRED";
    public static final String OUT_OF_STOCK = "OUT_OF_STOCK";
    public static final String LOW_STOCK = "LOW_STOCK";
    public static final String IN_STOCK = "IN_STOCK";

    public String computeStatus(Integer availableQuantity, Integer lowStockThreshold, LocalDate expiryDate) {
        if (expiryDate != null && !expiryDate.isAfter(LocalDate.now())) {
            return EXPIRED;
        }
        int avail = availableQuantity != null ? availableQuantity : 0;
        if (avail <= 0) {
            return OUT_OF_STOCK;
        }
        int threshold = lowStockThreshold != null ? lowStockThreshold : 10;
        if (avail <= threshold) {
            return LOW_STOCK;
        }
        return IN_STOCK;
    }

    public boolean isExpiringWithin30Days(LocalDate expiryDate) {
        if (expiryDate == null) return false;
        LocalDate today = LocalDate.now();
        return expiryDate.isAfter(today) && !expiryDate.isAfter(today.plusDays(30));
    }
}
