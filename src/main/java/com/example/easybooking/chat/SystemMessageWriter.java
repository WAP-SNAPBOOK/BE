package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
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
    public MessageResponse saveReservationCreated(Long chatRoomId, Long reservationId) {
        ChatRoom chatRoom = chatRoomReader.read(chatRoomId);

        Message message = Message.createReservationCreatedSystemMessage(
                chatRoomId,
                SYSTEM_SENDER_ID,
                reservationId
        );

        messageRepository.save(message);
        chatRoom.updateLastMessage(message.getId(), message.getSentAt());

        return MessageResponse.from(message, SYSTEM_SENDER_NAME);
    }
}