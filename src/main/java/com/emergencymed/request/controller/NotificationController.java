package com.emergencymed.request.controller;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.request.dto.ApiResponse;
import com.emergencymed.request.dto.NotificationResponse;
import com.emergencymed.request.dto.PageResponse;
import com.emergencymed.request.service.impl.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    public NotificationController(NotificationService notificationService, CurrentUserProvider currentUserProvider) {
        this.notificationService = notificationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<NotificationResponse> response = notificationService.getUserNotifications(
                currentUserProvider.userId(), unreadOnly, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        long count = notificationService.getUnreadCount(currentUserProvider.userId());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id, currentUserProvider.userId());
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead() {
        int count = notificationService.markAllAsRead(currentUserProvider.userId());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", Map.of("updatedCount", count)));
    }
}
