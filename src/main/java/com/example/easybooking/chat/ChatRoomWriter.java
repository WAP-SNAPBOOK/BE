package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void updateLastReadMessage(Long chatRoomId, Long userId, Long lastReadMessageId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 참여자 검증
        if (!chatRoom.isParticipant(userId)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }

        chatRoom.updateLastReadMessageId(lastReadMessageId, userId);
        chatRoomRepository.save(chatRoom);
    }
}
