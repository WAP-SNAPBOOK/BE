package com.example.easybooking.reservation.event;

import com.example.easybooking.chat.ChatRoomWriter;
import com.example.easybooking.chat.ChatTopicPublisher;
import com.example.easybooking.chat.SystemMessageWriter;
import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReservationChatEventListener {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomWriter chatRoomWriter;
    private final SystemMessageWriter systemMessageWriter;
    private final ChatTopicPublisher chatTopicPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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

        chatTopicPublisher.publishToRoom(chatRoom.getId(), msg);
    }
}
