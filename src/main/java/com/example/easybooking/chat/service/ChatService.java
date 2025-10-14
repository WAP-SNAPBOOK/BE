package com.example.easybooking.chat.service;

import com.example.easybooking.chat.ChatRoomReader;
import com.example.easybooking.chat.MessageWriter;
import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.dto.request.ChatMessageRequest;
import com.example.easybooking.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageWriter messageWriter;
    private final ChatRoomReader chatRoomReader;

    public ChatMessageResponse saveMessage(Long chatRoomId, Long userId, ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomReader.read(chatRoomId);

        if (!chatRoom.isParticipant(userId)) {
            throw new IllegalArgumentException("해당 채팅방에 참여 권한이 없습니다.");
        }

        return messageWriter.save(chatRoomId, userId, request);
    }
}
