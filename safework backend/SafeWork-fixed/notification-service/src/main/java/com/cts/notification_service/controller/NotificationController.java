package com.cts.notification_service.controller;

import com.cts.notification_service.entity.Notification;
import com.cts.notification_service.enums.NotificationCategory;
import com.cts.notification_service.service.INotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final INotificationService notificationService;

    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Authenticated users can view their own notifications.
     * userId in path should match the logged-in user (enforced downstream if needed).
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'HAZARD_OFFICER', 'SAFETY_OFFICER', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    /**
     * Internal endpoint: Called by other microservices (Hazard, Training) via Feign.
     * No JWT required — protected by network boundary (only accessible via service mesh).
     * Exposed as /notifications/internal/create in SecurityConfig as permitAll.
     */
    @PostMapping("/internal/create")
    public ResponseEntity<String> createNotification(
            @RequestParam Long userId,
            @RequestParam Long entityId,
            @RequestParam String message,
            @RequestParam String category) {

        NotificationCategory cat;
        try {
            cat = NotificationCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            cat = NotificationCategory.GENERAL;
        }
        notificationService.createNotification(userId, entityId, message, cat);
        return ResponseEntity.ok("Notification created");
    }

    /**
     * Mark a notification as read.
     */
    @PutMapping("/markAsRead/{notificationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'HAZARD_OFFICER', 'SAFETY_OFFICER', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }
}
