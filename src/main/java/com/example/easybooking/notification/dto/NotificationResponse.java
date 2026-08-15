package com.example.easybooking.notification.dto;

import com.example.easybooking.notification.domain.Notification;
import com.example.easybooking.notification.domain.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        NotificationType notificationType,
        String title,
        String body,
        Long reservationId,
        Long shopId,
        Long chatRoomId,
        Long messageId,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getReservationId(),
                notification.getShopId(),
                notification.getChatRoomId(),
                notification.getMessageId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
