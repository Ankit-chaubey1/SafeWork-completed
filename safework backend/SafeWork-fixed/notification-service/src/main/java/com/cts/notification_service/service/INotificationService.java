package com.cts.notification_service.service;

import com.cts.notification_service.entity.Notification;
import com.cts.notification_service.enums.NotificationCategory;

import java.util.List;

public interface INotificationService {

    void createNotification(
            Long userId,
            Long entityId,
            String message,
            NotificationCategory category
    );

    List<Notification> getUserNotifications(Long userId);

    void markAsRead(Long notificationId);
}