package com.example.easybooking.chat.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false)
    private String customerId;

}

