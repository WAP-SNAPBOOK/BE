package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.chat.request.ChatMessageRequest;
import com.example.easybooking.chat.response.ChatMessageResponse;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageWriter {
    private final MessageRepository messageRepository;
    private final UserReader userReader;

    public ChatMessageResponse save(Long chatRoomId, String providerId, ChatMessageRequest request) {
        User user = userReader.getUserByProviderId(providerId);
        Message message = Message.create(chatRoomId,user.getProviderId(), request.getMessage());
        messageRepository.save(message);
        ChatMessageResponse response = ChatMessageResponse.from(message);
        return response;
    }
}
