package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.errors.errorcode.ChatRoomErrorCode;
import com.example.easybooking.errors.exception.ChatRoomException;
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
        return chatRoomRepository.saveAndFlush(chatRoom);
    }

    @Transactional
    public void updateLastReadMessage(Long chatRoomId, Long userId, Long lastReadMessageId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomException(ChatRoomErrorCode.CHAT_ROOM_NOT_FOUND));

        // 참여자 검증
        if (!chatRoom.isParticipant(userId)) {
            throw new ChatRoomException(ChatRoomErrorCode.CHAT_PARTICIPANT_REQUIRED);
        }

        chatRoom.updateLastReadMessageId(lastReadMessageId, userId);
        chatRoomRepository.save(chatRoom);
    }
}
