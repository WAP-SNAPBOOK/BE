package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.ShopReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomWriter {
    private final ChatRoomRepository chatRoomRepository;
    private final ShopReader shopReader;

    public ChatRoom createChatRoom(Long shopId, Long userId) {
        Shop shop = shopReader.read(shopId);
        Long ownerId = shop.getOwnerId();
        ChatRoom chatRoom = ChatRoom.create(shopId, ownerId, userId);
        return chatRoomRepository.save(chatRoom);
    }
}
