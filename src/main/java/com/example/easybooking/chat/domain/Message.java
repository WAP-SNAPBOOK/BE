package com.example.easybooking.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(nullable = false)
    private Long senderId;


    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MessageType messageType;

    public static Message create(Long chatRoomId, Long senderId, String content) {
        Message message = new Message();
        message.chatRoomId = chatRoomId;
        message.senderId = senderId;
        message.content = content;
        message.sentAt = LocalDateTime.now();
        message.messageType = MessageType.TEXT;
        return message;
    }

    public static Message createImageMessgae(Long chatRoomId, Long senderId, String imageUrl) {
        Message message = new Message();
        message.chatRoomId = chatRoomId;
        message.senderId = senderId;
        message.imageUrl = imageUrl;
        message.sentAt = LocalDateTime.now();
        message.messageType = MessageType.IMAGE;
        return message;
    }

    public static Message createImageWithTextMessage(Long chatRoomId, Long senderId, String content, String imageUrl) {
        Message message = new Message();
        message.chatRoomId = chatRoomId;
        message.senderId = senderId;
        message.content = content;
        message.imageUrl = imageUrl;
        message.sentAt = LocalDateTime.now();
        message.messageType = MessageType.TEXT_IMAGE;
        return message;
    }
}
