package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.chat.dto.request.ChatMessageRequest;
import com.example.easybooking.chat.dto.response.ChatMessageResponse;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MessageWriter {
    private final MessageRepository messageRepository;
    private final UserReader userReader;

    public ChatMessageResponse save(Long chatRoomId, Long userId, ChatMessageRequest request) {
        User user = userReader.findById(userId);
        Message message = Message.create(chatRoomId,user.getId(), request.getMessage());
        messageRepository.save(message);
        ChatMessageResponse response = ChatMessageResponse.from(message);
        return response;
    }
}
