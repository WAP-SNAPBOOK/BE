package com.example.easybooking.notification;

import com.example.easybooking.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {
    private static final String NOTIFICATION_DESTINATION = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    public void publishToUser(Long recipientId, NotificationResponse notification) {
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                NOTIFICATION_DESTINATION,
                notification
        );
    }
}
