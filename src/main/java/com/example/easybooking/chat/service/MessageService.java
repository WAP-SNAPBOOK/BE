package com.example.easybooking.chat.service;

import com.example.easybooking.chat.ChatRoomReader;
import com.example.easybooking.chat.MessageReader;
import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageReader messageReader;
    private final ChatRoomReader chatRoomReader;

    public List<MessageResponse> getMessageHistory(
            Long chatRoomId,
            Long userId,
            Long cursor,
            int size) {

        ChatRoom chatRoom = chatRoomReader.read(chatRoomId);
        if (!chatRoom.isParticipant(userId)) {
            throw new IllegalArgumentException("해당 채팅방에 참여 권한이 없습니다.");
        }

        return messageReader.readMessages(chatRoomId, cursor, size);
    }

}
