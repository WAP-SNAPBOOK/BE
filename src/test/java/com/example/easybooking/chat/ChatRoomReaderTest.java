package com.example.easybooking.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.domain.MessageType;
import com.example.easybooking.chat.dto.response.ChatRoomListResponse;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatRoomReaderTest {

    @Mock ChatRoomRepository chatRoomRepository;
    @Mock ShopReader shopReader;
    @Mock UserReader userReader;
    @Mock MessageReader messageReader;

    private ChatRoomReader chatRoomReader;

    @BeforeEach
    void setUp() {
        chatRoomReader = new ChatRoomReader(chatRoomRepository, shopReader, userReader, messageReader);
    }

    @Test
    void getChatRoomList_includesLastMessageType() {
        long ownerId = 1L;
        long customerId = 2L;
        long shopId = 10L;
        long chatRoomId = 100L;
        long lastMessageId = 1000L;
        LocalDateTime sentAt = LocalDateTime.of(2026, 6, 13, 20, 13, 28);

        ChatRoom chatRoom = ChatRoom.create(shopId, ownerId, customerId);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        chatRoom.updateLastMessage(lastMessageId, sentAt);

        Message lastMessage = Message.createReservationSystemMessage(
                chatRoomId,
                0L,
                2000L,
                60,
                MessageType.RESERVATION_CREATED
        );

        Shop shop = mock(Shop.class);
        when(shop.getBusinessName()).thenReturn("테스트");
        User otherUser = mock(User.class);
        when(otherUser.getName()).thenReturn("테스트 매장");

        when(chatRoomRepository.findChatRooms(customerId)).thenReturn(List.of(chatRoom));
        when(messageReader.read(lastMessageId)).thenReturn(lastMessage);
        when(messageReader.countUnreadMessages(chatRoomId, 0L, customerId)).thenReturn(1);
        when(userReader.read(ownerId)).thenReturn(otherUser);
        when(shopReader.read(shopId)).thenReturn(shop);

        List<ChatRoomListResponse> responses = chatRoomReader.getChatRoomList(customerId);

        assertThat(responses).hasSize(1);
        ChatRoomListResponse response = responses.get(0);
        assertThat(response.getLastMessageSenderId()).isZero();
        assertThat(response.getLastMessageType()).isEqualTo(MessageType.RESERVATION_CREATED);
        assertThat(response.getLastMessageContent()).isNull();
        assertThat(response.getLastMessageAt()).isEqualTo(sentAt);
        assertThat(response.getUnreadCount()).isEqualTo(1);
    }
}
