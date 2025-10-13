package com.example.easybooking.chat.service;

import com.example.easybooking.chat.ChatRoomWriter;
import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.dto.response.ChatRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.easybooking.chat.ChatRoomReader;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomReader chatRoomReader;
    private final ChatRoomWriter chatRoomWriter;

    public ChatRoomResponse getChatRoom(Long shopId, Long userId) {
        Optional<ChatRoom> chatRoomOpt = chatRoomReader.find(shopId, userId);
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            return ChatRoomResponse.of(chatRoom, false);
        } else {
            ChatRoom newChatRoom = createChatRoom(shopId, userId);
            return ChatRoomResponse.of(newChatRoom, true);
        }
    }

    public ChatRoom createChatRoom(Long shopId, Long userId) {
        return chatRoomWriter.createChatRoom(shopId, userId);
    }

}
