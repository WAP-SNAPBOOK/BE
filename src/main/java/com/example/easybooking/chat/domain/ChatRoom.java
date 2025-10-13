package com.example.easybooking.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shopId;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastMessageAt;

    public static ChatRoom create(Long shopId, Long ownerId, Long customerId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.shopId = shopId;
        chatRoom.ownerId = ownerId;
        chatRoom.customerId = customerId;
        chatRoom.createdAt = LocalDateTime.now();
        return chatRoom;
    }

    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public boolean isParticipant(Long userId) {
        return this.ownerId.equals(userId) || this.customerId.equals(userId);
    }
}

