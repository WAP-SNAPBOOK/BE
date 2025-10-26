package com.example.easybooking.chat.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomListResponse {
    private Long chatRoomId;
    private Long shopId;
    private String shopBusinessName;

    private Long otherUserId;
    private String otherUserName;

    private Long lastMessageSenderId;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;

    private int unreadCount;

}
