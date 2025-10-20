package com.example.easybooking.shop;

import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.CreateShopRequest;
import com.example.easybooking.shop.dto.CreateShopResponse;
import com.example.easybooking.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopWriter {
    private final ShopRepository shopRepository;
    private final ShopReader shopreader;

    public Shop save(Shop shop) {
        return shopRepository.save(shop);
    }

    public CreateShopResponse create(Long ownerId, CreateShopRequest request) {
        if(shopreader.isExist(ownerId)){
            throw new IllegalStateException("이미 매장을 등록한 사용자입니다");
        }
        Shop shop = Shop.create(ownerId,request);
        shopRepository.save(shop);
        return CreateShopResponse.of(ownerId,shop);
    }
}
