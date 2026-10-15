package com.emergencymed.common.contract;

import com.emergencymed.common.dto.*;

import com.emergencymed.common.enums.NotificationType;
import com.emergencymed.common.enums.Role;

public interface NotificationPublisher {
    void publish(Long userId, String type, String title, String message, Long requestId);

    default void publish(Long userId, NotificationType type, String title, String message, Long requestId) {
        publish(userId, type != null ? type.name() : "GENERAL", title, message, requestId);
    }

    default void publish(Long userId, Long requestId, NotificationType type, String title, String message) {
        publish(userId, type != null ? type.name() : "GENERAL", title, message, requestId);
    }

    default void publishToRole(Role role, Long requestId, NotificationType type, String title, String message) {
        // default no-op if role-based fan-out is not active
    }
}
