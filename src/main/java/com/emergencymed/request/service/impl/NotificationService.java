package com.emergencymed.request.service.impl;

import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.request.dto.NotificationResponse;
import com.emergencymed.request.dto.PageResponse;
import com.emergencymed.request.entity.Notification;
import com.emergencymed.request.exception.AccessForbiddenException;
import com.emergencymed.request.exception.ResourceNotFoundException;
import com.emergencymed.request.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService implements NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void publish(Long userId, String type, String title, String message, Long requestId) {
        log.info("[NOTIFICATION PUBLISH] User: {}, Type: {}, Title: {}, RequestId: {}", userId, type, title, requestId);
        Notification notification = new Notification(userId, requestId, type, title, message, LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(Long userId, Boolean unreadOnly, Pageable pageable) {
        Page<Notification> page = (Boolean.TRUE.equals(unreadOnly))
                ? notificationRepository.findByUserIdAndIsReadFalse(userId, pageable)
                : notificationRepository.findByUserId(userId, pageable);

        Page<NotificationResponse> mapped = page.map(this::mapToResponse);
        return PageResponse.from(mapped);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!notification.getUserId().equals(userId)) {
            throw new AccessForbiddenException("You do not own this notification");
        }
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
    }

    private NotificationResponse mapToResponse(Notification n) {
        boolean isEmergency = "REQUEST_CREATED".equals(n.getType()) || "CRITICAL".equals(n.getTitle()) || n.getType().contains("EMERGENCY") || "LOW_STOCK".equals(n.getType());
        return new NotificationResponse(
                n.getId(),
                n.getRequestId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt(),
                isEmergency
        );
    }
}
