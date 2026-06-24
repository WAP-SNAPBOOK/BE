package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.domain.MessageType;
import com.example.easybooking.chat.domain.ReservationChangeSnapshot;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SystemMessageWriter {
    private static final Long SYSTEM_SENDER_ID = 0L;
    private static final String SYSTEM_SENDER_NAME = "SYSTEM";

    private final MessageRepository messageRepository;
    private final ChatRoomReader chatRoomReader;

    @Transactional
    public MessageResponse saveReservationMessage(
            Long chatRoomId,
            Long reservationId,
            Integer durationMinutes,
            String content,
            ReservationChangeSnapshot reservationChange,
            MessageType messageType
    ) {
        ChatRoom chatRoom = chatRoomReader.read(chatRoomId);
        String messageContent = resolveContent(content, durationMinutes, messageType);

        Message message = Message.createReservationSystemMessage(
                chatRoomId,
                SYSTEM_SENDER_ID,
                reservationId,
                durationMinutes,
                messageContent,
                reservationChange,
                messageType
        );
        messageRepository.save(message);
        chatRoom.updateLastMessage(message.getId(), message.getSentAt());

        return MessageResponse.from(message, SYSTEM_SENDER_NAME);
    }

    private String resolveContent(String content, Integer durationMinutes, MessageType messageType) {
        if (content != null && !content.isBlank()) {
            return content;
        }

        return switch (messageType) {
            case RESERVATION_CREATED -> "예약이 접수되었습니다.";
            case RESERVATION_CONFIRMED ->
                    durationMinutes != null ? "예약이 확정되었습니다. 소요시간 " + durationMinutes + "분" : "예약이 확정되었습니다.";
            case RESERVATION_REJECTED -> "예약이 거절되었습니다.";
            case RESERVATION_CANCELED -> "예약이 취소되었습니다.";
            case RESERVATION_UPDATED -> "예약이 수정되었습니다.";
            default -> "예약 상태가 변경되었습니다.";
        };
    }
}
