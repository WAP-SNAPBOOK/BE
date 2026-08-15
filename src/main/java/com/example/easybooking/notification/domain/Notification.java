package com.example.easybooking.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_notifications_recipient_message",
                columnNames = {"recipient_id", "message_id"}
        )
)
@Getter
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    private Long reservationId;
    private Long shopId;
    private Long chatRoomId;
    private Long messageId;
    private LocalDateTime readAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Notification createReservationNotification(
            Long recipientId,
            Long actorId,
            NotificationType notificationType,
            String body,
            Long reservationId,
            Long shopId,
            Long chatRoomId,
            Long messageId
    ) {
        Notification notification = new Notification();
        notification.recipientId = recipientId;
        notification.actorId = actorId;
        notification.notificationType = notificationType;
        notification.title = notificationType.getTitle();
        notification.body = body;
        notification.reservationId = reservationId;
        notification.shopId = shopId;
        notification.chatRoomId = chatRoomId;
        notification.messageId = messageId;
        notification.createdAt = LocalDateTime.now();
        return notification;
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead(LocalDateTime readAt) {
        if (this.readAt == null) {
            this.readAt = readAt;
        }
    }
}
