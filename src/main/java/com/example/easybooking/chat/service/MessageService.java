package com.example.easybooking.chat.service;

import com.example.easybooking.chat.ChatRoomReader;
import com.example.easybooking.chat.MessageReader;
import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.user.UserReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageReader messageReader;
    private final ChatRoomReader chatRoomReader;
    private final UserReader userReader;

    public List<MessageResponse> getMessageHistory(
            Long chatRoomId,
            Long userId,
            Long cursor,
            int size) {
        ChatRoom chatRoom = chatRoomReader.read(chatRoomId);
        if (!chatRoom.isParticipant(userId)) {
            throw new IllegalArgumentException("해당 채팅방에 참여 권한이 없습니다.");
        }

        String ownerName = userReader.read(chatRoom.getOwnerId()).getName();
        String customerName = userReader.read(chatRoom.getCustomerId()).getName();

        Map<Long, String> nameMap = new HashMap<>();
        nameMap.put(chatRoom.getOwnerId(), ownerName);
        nameMap.put(chatRoom.getCustomerId(), customerName);

        List<Message> messages = messageReader.readMessages(chatRoom, cursor, size, userId);

        return messages.stream()
                .map(m -> MessageResponse.from(m, nameMap.getOrDefault(m.getSenderId(), "알 수 없음")))
                .toList();
    }

}
