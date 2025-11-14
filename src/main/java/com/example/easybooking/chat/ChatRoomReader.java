package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.dto.response.ChatRoomListResponse;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.errors.errorcode.ChatRoomErrorCode;
import com.example.easybooking.errors.exception.ChatRoomException;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomReader {
    private final ChatRoomRepository chatRoomRepository;
    private final ShopReader shopReader;
    private final UserReader userReader;
    private final MessageReader messageReader;

    public ChatRoom read(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomException(ChatRoomErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    public Optional<ChatRoom> find(Long shopId, Long userId) {
        return chatRoomRepository.findByShopIdAndCustomerId(shopId, userId);
    }

    public List<ChatRoomListResponse> getChatRoomList(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRooms(userId);

        return chatRooms.stream()
                .map(chatRoom -> convertToChatRoomListResponse(userId, chatRoom))
                .toList();
    }

    private ChatRoomListResponse convertToChatRoomListResponse(Long userId, ChatRoom chatRoom) {
        LastMessageInfo lastMessageInfo = getLastMessageInfo(chatRoom);
        int unreadCount = calculateUnreadCount(userId, chatRoom);
        OtherUserInfo otherUserInfo = getOtherUserInfo(userId, chatRoom);
        Shop shop = shopReader.read(chatRoom.getShopId());

        return ChatRoomListResponse.builder()
                .chatRoomId(chatRoom.getId())
                .shopId(chatRoom.getShopId())
                .shopBusinessName(shop.getBusinessName())
                .otherUserId(otherUserInfo.userId())
                .otherUserName(otherUserInfo.userName())
                .lastMessageSenderId(lastMessageInfo.senderId())
                .lastMessageContent(lastMessageInfo.content())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .unreadCount(unreadCount)
                .build();
    }

    private LastMessageInfo getLastMessageInfo(ChatRoom chatRoom) {
        if (chatRoom.getLastMessageId() == null || chatRoom.getLastMessageId() == 0) {
            return new LastMessageInfo(null, null);
        }

        Message lastMessage = messageReader.read(chatRoom.getLastMessageId());
        return new LastMessageInfo(
                lastMessage.getSenderId(),
                lastMessage.getContent()
        );
    }

    private int calculateUnreadCount(Long userId, ChatRoom chatRoom) {
        Long lastReadMessageId = chatRoom.getLastReadMessageId(userId);
        return messageReader.countUnreadMessages(
                chatRoom.getId(),
                lastReadMessageId,
                userId
        );
    }

    private OtherUserInfo getOtherUserInfo(Long userId, ChatRoom chatRoom) {
        Long otherUserId = determineOtherUserId(userId, chatRoom);
        User otherUser = userReader.read(otherUserId);
        return new OtherUserInfo(otherUserId, otherUser.getName());
    }

    private Long determineOtherUserId(Long userId, ChatRoom chatRoom) {
        return chatRoom.getOwnerId().equals(userId)
                ? chatRoom.getCustomerId()
                : chatRoom.getOwnerId();
    }

    private record LastMessageInfo(Long senderId, String content) {
    }

    private record OtherUserInfo(Long userId, String userName) {
    }
}
