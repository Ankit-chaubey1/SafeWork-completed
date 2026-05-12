package com.cts.notification_service.service;

import com.cts.notification_service.entity.Notification;
import com.cts.notification_service.enums.NotificationCategory;
import com.cts.notification_service.enums.NotificationStatus;
import com.cts.notification_service.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void createNotification(
            Long userId,
            Long entityId,
            String message,
            NotificationCategory category) {

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setEntityId(entityId);
        notification.setMessage(message);
        notification.setCategory(category);
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setCreatedDate(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found with id: " + notificationId)
                );

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }
}