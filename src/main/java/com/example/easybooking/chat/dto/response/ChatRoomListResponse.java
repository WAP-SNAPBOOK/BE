package com.example.easybooking.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private String lastMessageContent;
    private LocalDateTime lastMessageAt;

    private int unreadCount;

}
