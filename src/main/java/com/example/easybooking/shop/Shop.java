package com.example.easybooking.shop;

import jakarta.persistence.*;

@Entity
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long OwnerId;

    @Column(nullable = false)
    private String businessName;

    @Column
    private String businessNumber;

    @Column
    private String address;

}
