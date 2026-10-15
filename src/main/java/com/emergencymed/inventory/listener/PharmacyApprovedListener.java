package com.emergencymed.inventory.listener;

import com.emergencymed.common.events.PharmacyApprovedEvent;
import com.emergencymed.inventory.service.PharmacySettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PharmacyApprovedListener {
    private static final Logger log = LoggerFactory.getLogger(PharmacyApprovedListener.class);
    private final PharmacySettingsService settingsService;

    public PharmacyApprovedListener(PharmacySettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @EventListener
    public void handlePharmacyApproved(PharmacyApprovedEvent event) {
        log.info("Received PharmacyApprovedEvent for pharmacy ID: {}", event.getPharmacyId());
        settingsService.getOrCreateSettings(event.getPharmacyId());
    }
}
