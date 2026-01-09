package com.example.easybooking.chat.dto.response;

import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.domain.MessageType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MessageResponse {
    private MessageType messageType;
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String message;
    private String imageUrl;
    private LocalDateTime sentAt;
    private Long roomId;
    private Long reservationId;

    public static MessageResponse from(Message message, String senderName) {
        return MessageResponse.builder()
                .messageType(message.getMessageType())
                .messageId(message.getId())
                .senderId(message.getSenderId())
                .senderName(senderName)
                .message(message.getContent())
                .imageUrl(message.getImageUrl())
                .sentAt(message.getSentAt())
                .roomId(message.getChatRoomId())
                .reservationId(message.getReservationId())
                .build();
    }
}
