package com.example.easybooking.reservation.event;

import com.example.easybooking.notification.dto.NotificationResponse;

public record ReservationNotificationSavedEvent(
        Long recipientId,
        NotificationResponse notification
) {
}
