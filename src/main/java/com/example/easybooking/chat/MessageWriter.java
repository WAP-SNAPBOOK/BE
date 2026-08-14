package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.dto.request.ChatMessageRequest;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.errors.errorcode.ChatErrorCode;
import com.example.easybooking.errors.exception.ChatException;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MessageWriter {
    private final MessageRepository messageRepository;
    private final UserReader userReader;
    private final ChatRoomReader chatRoomReader;

    @Transactional
    public MessageResponse save(Long chatRoomId, Long userId, ChatMessageRequest request) {
        User user = userReader.read(userId);
        ChatRoom chatRoom = chatRoomReader.readForUpdate(chatRoomId);
        Message message;

        if (!request.hasImage() && !request.hasText()) {
            throw new ChatException(ChatErrorCode.MESSAGE_CONTENT_INVALID);
        } else if (!request.hasImage()) {
            message = Message.create(
                    chatRoomId,
                    userId,
                    request.getMessage()
            );
        } else if (request.hasText()) {
            message = Message.createImageWithTextMessage(
                    chatRoomId,
                    userId,
                    request.getMessage(),
                    request.getImageUrl()
            );
        } else {
            message = Message.createImageMessgae(
                    chatRoomId,
                    userId,
                    request.getImageUrl()
            );
        }

        messageRepository.save(message);
        chatRoom.updateLastMessage(message.getId(), LocalDateTime.now());
        MessageResponse response = MessageResponse.from(message, user.getName());
        return response;

    }
}
