package com.cts.notification_service.entity;

import com.cts.notification_service.enums.NotificationCategory;
import com.cts.notification_service.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private LocalDateTime createdDate;

}