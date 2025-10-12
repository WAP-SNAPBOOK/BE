package com.example.easybooking.chat.domain;

import jakarta.persistence.*;

@Entity
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private Long customerId;

}

