package com.example.easybooking.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;

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
    private Long lastMessageId;

    @Column
    private LocalDateTime lastMessageAt;

    @Column
    private Long ownerLastReadMessageId;

    @Column
    private Long customerLastReadMessageId;

    public static ChatRoom create(Long shopId, Long ownerId, Long customerId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.shopId = shopId;
        chatRoom.ownerId = ownerId;
        chatRoom.customerId = customerId;
        chatRoom.createdAt = LocalDateTime.now();
        chatRoom.lastMessageId = 0L;
        chatRoom.ownerLastReadMessageId = 0L;
        chatRoom.customerLastReadMessageId = 0L;
        return chatRoom;
    }

    public Long getLastReadMessageId(Long userId) {
        if (this.ownerId.equals(userId)) {
            return this.ownerLastReadMessageId != null ? this.ownerLastReadMessageId : 0L;
        } else if (this.customerId.equals(userId)) {
            return this.customerLastReadMessageId != null ? this.customerLastReadMessageId : 0L;
        }
        return 0L;
    }

    public void updateLastMessage(Long messageId, LocalDateTime sentAt) {
        this.lastMessageId = messageId;
        this.lastMessageAt = sentAt;
    }

    public void updateLastReadMessageId(Long messageId, Long userId) {
        if (this.ownerId.equals(userId)) {
            this.ownerLastReadMessageId = messageId;
        } else if (this.customerId.equals(userId)) {
            this.customerLastReadMessageId = messageId;
        }
    }

    public boolean isParticipant(Long userId) {
        return this.ownerId.equals(userId) || this.customerId.equals(userId);
    }
}

