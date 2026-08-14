package com.example.easybooking.reservation.event;

import com.example.easybooking.chat.ChatTopicPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationChatPublishListener {
    private final ChatTopicPublisher chatTopicPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationMessageSaved(ReservationMessageSavedEvent event) {
        try {
            chatTopicPublisher.publishToRoom(event.chatRoomId(), event.message());
        } catch (RuntimeException e) {
            log.error(
                    "예약 시스템 메시지 웹소켓 발행 실패 chatRoomId={} messageId={}",
                    event.chatRoomId(),
                    event.message().getMessageId(),
                    e
            );
        }
    }
}
