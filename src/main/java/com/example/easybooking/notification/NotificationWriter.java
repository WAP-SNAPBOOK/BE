package com.example.easybooking.notification;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.notification.domain.Notification;
import com.example.easybooking.notification.domain.NotificationType;
import com.example.easybooking.notification.dto.NotificationResponse;
import com.example.easybooking.notification.repository.NotificationRepository;
import com.example.easybooking.reservation.event.ReservationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationWriter {
    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public NotificationResponse saveReservationNotification(
            ReservationEvent event,
            ChatRoom chatRoom,
            MessageResponse message
    ) {
        Long recipientId = chatRoom.getOtherParticipantId(event.actorUserId());
        NotificationType notificationType = NotificationType.from(event.messageType());

        Notification notification = Notification.createReservationNotification(
                recipientId,
                event.actorUserId(),
                notificationType,
                message.getMessage(),
                event.reservationId(),
                event.shopId(),
                chatRoom.getId(),
                message.getMessageId()
        );

        return NotificationResponse.from(notificationRepository.save(notification));
    }
}
