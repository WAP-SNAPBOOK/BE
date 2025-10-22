package com.example.easybooking.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    public static Message create(Long chatRoomId,Long senderId,String senderName, String content) {
        Message message = new Message();
        message.chatRoomId = chatRoomId;
        message.senderId = senderId;
        message.senderName = senderName;
        message.content = content;
        message.sentAt = LocalDateTime.now();
        return message;
    }
}
