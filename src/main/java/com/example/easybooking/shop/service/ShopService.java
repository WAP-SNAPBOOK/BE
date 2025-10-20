package com.example.easybooking.shop.service;

import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.ShopWriter;
import com.example.easybooking.shop.dto.CreateShopRequest;
import com.example.easybooking.shop.dto.CreateShopResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopWriter shopwriter;

    @Transactional
    public CreateShopResponse createShop(Long ownerId, CreateShopRequest request) {
        return shopwriter.create(ownerId,request);
    }
}
