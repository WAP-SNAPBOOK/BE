package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.dto.response.ChatRoomListResponse;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatRoomReader {
    private final ChatRoomRepository chatRoomRepository;
    private final ShopReader shopReader;
    private final UserReader userReader;
    private final MessageReader messageReader;

    public ChatRoom read(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
    }

    public Optional<ChatRoom> find(Long shopId, Long userId) {
        return chatRoomRepository.findByShopIdAndCustomerId(shopId, userId);
    }

    public List<ChatRoomListResponse> getChatRoomList(Long userId) {
        List<ChatRoom> chatRooms = getChatRooms(userId);
        List<ChatRoomListResponse> chatRoomListResponses = new ArrayList<>();
        if(chatRooms.isEmpty()) return chatRoomListResponses;
        for (ChatRoom chatRoom : chatRooms) {
            chatRoomListResponses.add(from(userId,chatRoom));
        }
        return chatRoomListResponses;
    }

    public List<ChatRoom> getChatRooms(Long userId) {
        return chatRoomRepository.findChatRooms(userId);
    }

    public ChatRoomListResponse from(Long userId,ChatRoom chatRoom) {
        Message lastMessage = null;
        if (chatRoom.getLastMessageId() != null && chatRoom.getLastMessageId() > 0) {
            lastMessage = messageReader.read(chatRoom.getLastMessageId());
        }

        Long lastReadMessageId = chatRoom.getLastReadMessageId(userId);
        int unreadCount = messageReader.countUnreadMessages(
                chatRoom.getId(),
                lastReadMessageId,
                userId  // 내가 보낸 메시지는 제외
        );

        Long otherUserId = chatRoom.getOwnerId().equals(userId)
                ? chatRoom.getCustomerId()
                : chatRoom.getOwnerId();
        User otherUser = userReader.read(otherUserId);

        Shop shop = shopReader.read(chatRoom.getShopId());

        return ChatRoomListResponse.builder()
                .chatRoomId(chatRoom.getId())
                .shopId(chatRoom.getShopId())
                .shopBusinessName(shop.getBusinessName())
                .otherUserId(otherUserId)
                .otherUserName(otherUser.getName())
                .lastMessageContent(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageAt(chatRoom.getLastMessageAt())
                .unreadCount(unreadCount)
                .build();
    }
}
