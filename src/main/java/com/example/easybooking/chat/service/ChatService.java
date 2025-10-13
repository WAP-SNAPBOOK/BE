package com.example.easybooking.chat.service;

import com.example.easybooking.chat.MessageWriter;
import com.example.easybooking.chat.dto.request.ChatMessageRequest;
import com.example.easybooking.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageWriter messageWriter;
    public ChatMessageResponse saveMessage(Long chatRoomId, Long userId, ChatMessageRequest request) {
        return messageWriter.save(chatRoomId, userId, request);
    }
}
