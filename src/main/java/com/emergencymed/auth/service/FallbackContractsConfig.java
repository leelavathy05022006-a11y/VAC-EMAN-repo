package com.emergencymed.auth.service;

import com.emergencymed.common.contract.*;
import com.emergencymed.common.dto.*;
import com.emergencymed.common.enums.DeliveryStatus;
import com.emergencymed.common.enums.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
public class FallbackContractsConfig {

    private static final Logger log = LoggerFactory.getLogger(FallbackContractsConfig.class);

    @Bean
    @ConditionalOnMissingBean(NotificationPublisher.class)
    public NotificationPublisher fallbackNotificationPublisher() {
        return (userId, type, title, message, requestId) -> {
            log.info("[FALLBACK NOTIFICATION] User: {}, Type: {}, Title: '{}', Message: '{}', Request: {}",
                    userId, type, title, message, requestId);
        };
    }

    @Bean
    @ConditionalOnMissingBean(MedicineLookup.class)
    public MedicineLookup fallbackMedicineLookup() {
        return new MedicineLookup() {
            @Override
            public Optional<MedicineDto> findById(Long id) {
                return Optional.of(new MedicineDto(
                        id, "Paracetamol 500mg", "Acetaminophen", "Tablet", "500mg", "GSK", false, BigDecimal.valueOf(25.00)
                ));
            }

            @Override
            public List<MedicineDto> searchByName(String q, int limit) {
                return List.of(
                        new MedicineDto(1L, "Paracetamol 500mg", "Acetaminophen", "Tablet", "500mg", "GSK", false, BigDecimal.valueOf(25.00)),
                        new MedicineDto(2L, "Amoxicillin 250mg", "Amoxicillin", "Capsule", "250mg", "Cipla", true, BigDecimal.valueOf(45.00))
                );
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(InventoryQueryService.class)
    public InventoryQueryService fallbackInventoryQueryService() {
        return new InventoryQueryService() {
            @Override
            public List<MedicineOfferDto> findOffers(Long medicineId, int qty, List<Long> pharmacyIds) {
                return Collections.emptyList();
            }

            @Override
            public AlternativesDto findAlternatives(Long medicineId, List<Long> pharmacyIds) {
                return new AlternativesDto();
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(InventoryService.class)
    public InventoryService fallbackInventoryService() {
        return new InventoryService() {
            @Override
            public Long reserve(Long pharmacyId, Long medicineId, int qty, String refCode) {
                log.info("[FALLBACK INVENTORY] Reserved {} of med {} at pharmacy {} (ref: {})", qty, medicineId, pharmacyId, refCode);
                return 1L;
            }

            @Override
            public void commit(Long inventoryId, int qty, String refCode) {
                log.info("[FALLBACK INVENTORY] Committed {} of inv {} (ref: {})", qty, inventoryId, refCode);
            }

            @Override
            public void release(Long inventoryId, int qty, String refCode) {
                log.info("[FALLBACK INVENTORY] Released {} of inv {} (ref: {})", qty, inventoryId, refCode);
            }

            @Override
            public void restore(Long inventoryId, int qty, String refCode) {
                log.info("[FALLBACK INVENTORY] Restored {} of inv {} (ref: {})", qty, inventoryId, refCode);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(PharmacySettingsLookup.class)
    public PharmacySettingsLookup fallbackPharmacySettingsLookup() {
        return pharmacyId -> Optional.of(new PharmacySettingsDto(
                pharmacyId, true, BigDecimal.valueOf(10.0), BigDecimal.valueOf(30.00)
        ));
    }

    @Bean
    @ConditionalOnMissingBean(DeliveryAvailabilityQuery.class)
    public DeliveryAvailabilityQuery fallbackDeliveryAvailabilityQuery() {
        return pharmacyId -> DeliveryStatus.AVAILABLE;
    }
}
