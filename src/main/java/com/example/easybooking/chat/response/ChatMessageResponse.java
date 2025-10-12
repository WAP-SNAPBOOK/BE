package com.example.easybooking.chat.response;

import com.example.easybooking.chat.domain.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChatMessageResponse {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String message;
    private LocalDateTime sentAt;
    private Long roomId;

    public static ChatMessageResponse from(Message message){
        return ChatMessageResponse.builder()
                .messageId(message.getId())
                .senderId(message.getSenderId())
                .message(message.getContent())
                .sentAt(message.getSentAt())
                .roomId(message.getChatRoomId())
                .build();
    }
}
