package com.example.easybooking.shop.domain;

import com.example.easybooking.shop.dto.CreateShopRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long ownerId;

    @Column(nullable = false)
    private String businessName;

    @Column
    private String businessNumber;

    @Column
    private String address;

    public static Shop create(Long ownerId, CreateShopRequest request) {
        Shop shop = new Shop();
        shop.ownerId = ownerId;
        shop.businessName = request.getBusinessName();
        shop.businessNumber = request.getBusinessNumber();
        shop.address = request.getAddress();
        return shop;
    }

}
