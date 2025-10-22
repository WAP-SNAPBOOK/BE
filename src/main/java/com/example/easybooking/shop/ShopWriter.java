package com.example.easybooking.shop;

import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.CreateShopRequest;
import com.example.easybooking.shop.dto.CreateShopResponse;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopWriter {
    private final ShopRepository shopRepository;
    private final ShopReader shopReader;
    private final UserReader userReader;

    public Shop save(Shop shop) {
        return shopRepository.save(shop);
    }

    public CreateShopResponse create(Long ownerId, CreateShopRequest request) {
        if(shopReader.isExist(ownerId)){
            throw new IllegalStateException("이미 매장을 등록한 사용자입니다");
        }
        User user = userReader.read(ownerId);
        if(user.getUserType() != UserType.OWNER){
            throw new IllegalStateException("점주 사용자가 아니면 매장을 등록할 수 없습니다.");
        }
        Shop shop = Shop.create(ownerId,request);
        shopRepository.save(shop);
        return CreateShopResponse.of(ownerId,shop);
    }
}
