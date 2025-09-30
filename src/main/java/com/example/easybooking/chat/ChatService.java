package com.example.easybooking.chat;

import com.example.easybooking.chat.request.ChatMessageRequest;
import com.example.easybooking.chat.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageWriter messageWriter;
    public ChatMessageResponse saveMessage(Long chatRoomId, String providerId, ChatMessageRequest request) {
        return messageWriter.save(chatRoomId, providerId, request);
    }
}
