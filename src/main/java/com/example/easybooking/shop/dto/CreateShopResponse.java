package com.example.easybooking.shop.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateShopResponse {
    private Long ownerId;
    private Long shopId;
    private String businessName;
    private String address;
    private String businessNumber;
}
