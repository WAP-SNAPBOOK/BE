package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.chat.dto.request.ChatMessageRequest;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MessageWriter {
    private final MessageRepository messageRepository;
    private final UserReader userReader;
    private final ChatRoomReader chatRoomReader;

    @Transactional
    public MessageResponse save(Long chatRoomId, Long userId, ChatMessageRequest request) {
        User user = userReader.read(userId);
        Message message = Message.create(
                chatRoomId,
                userId,
                request.getMessage()
        );
        messageRepository.save(message);
        ChatRoom chatRoom = chatRoomReader.read(chatRoomId);
        chatRoom.updateLastMessage(message.getId(), LocalDateTime.now());
        MessageResponse response = MessageResponse.from(message, user.getName());
        return response;
    }
}
