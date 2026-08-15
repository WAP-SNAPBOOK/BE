package com.example.easybooking.reservation.event;

import com.example.easybooking.chat.ChatRoomWriter;
import com.example.easybooking.chat.SystemMessageWriter;
import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.notification.NotificationWriter;
import com.example.easybooking.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReservationChatEventListener {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomWriter chatRoomWriter;
    private final SystemMessageWriter systemMessageWriter;
    private final NotificationWriter notificationWriter;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void onReservationEvent(ReservationEvent event) {
        ChatRoom chatRoom = chatRoomRepository
                .findByShopIdAndCustomerId(event.shopId(), event.customerId())
                .orElseGet(() -> chatRoomWriter.createChatRoom(event.shopId(), event.customerId()));

        MessageResponse msg = systemMessageWriter.saveReservationMessage(
                chatRoom.getId(),
                event.reservationId(),
                event.durationMinutes(),
                event.content(),
                event.reservationChange(),
                event.messageType()
        );

        NotificationResponse notification = notificationWriter.saveReservationNotification(event, chatRoom, msg);

        eventPublisher.publishEvent(new ReservationMessageSavedEvent(chatRoom.getId(), msg));
        eventPublisher.publishEvent(new ReservationNotificationSavedEvent(
                chatRoom.getOtherParticipantId(event.actorUserId()),
                notification
        ));
    }
}
