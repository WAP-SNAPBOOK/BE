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
    @Column(length = 20, unique = true) // 초기엔 nullable 허용, 백필 후 not null 권장
    private String publicCode;

    @Column(length = 50, unique = true) // 선택적 바니티
    private String slug;

    public void assignPublicCode(String code) {
        this.publicCode = code;
    }

    public void updateSlug(String slug) {
        this.slug = slug;
    }

    public static Shop create(Long ownerId, CreateShopRequest request) {
        Shop shop = new Shop();
        shop.ownerId = ownerId;
        shop.businessName = request.getBusinessName();
        shop.businessNumber = request.getBusinessNumber();
        shop.address = request.getAddress();
        return shop;
    }

}
