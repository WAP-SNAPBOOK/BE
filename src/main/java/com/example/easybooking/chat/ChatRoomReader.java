package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatRoomReader {
    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom read(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
    }

    public Optional<ChatRoom> find(Long shopId, Long userId) {
        return chatRoomRepository.findByShopIdAndCustomerId(shopId, userId);
    }
}
