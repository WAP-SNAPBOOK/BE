package com.example.easybooking.shop.service;

import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.ShopWriter;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.CreateShopRequest;
import com.example.easybooking.shop.dto.CreateShopResponse;
import com.example.easybooking.shop.dto.LinkInfoResponse;
import com.example.easybooking.shop.dto.SlugUpdateRequest;
import com.example.easybooking.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopWriter shopwriter;
    private final ShopReader shopReader;
    private final ShopRepository shopRepository;

    @Transactional
    public CreateShopResponse createShop(Long ownerId, CreateShopRequest request) {
        return shopwriter.create(ownerId,request);
    }

    @Transactional(readOnly = true)
    public LinkInfoResponse getLinkInfo(Long ownerId) {
        Shop shop = shopReader.readByOwnerId(ownerId);
        String slugOrCode = (shop.getSlug() != null && !shop.getSlug().isBlank())
                ? shop.getSlug() : shop.getPublicCode();
        return LinkInfoResponse.of("/s/" + slugOrCode, shop.getSlug(), shop.getPublicCode());
    }

    @Transactional
    public LinkInfoResponse updateSlug(Long ownerId, SlugUpdateRequest request) {
        Shop shop = shopReader.readByOwnerId(ownerId);
        // 예약어/충돌 검사
        shopRepository.findBySlug(request.getSlug()).ifPresent(exist -> {
            if (!exist.getId().equals(shop.getId())) {
                throw new IllegalArgumentException("Slug already in use");
            }
        });
        shop.updateSlug(request.getSlug());
        shopRepository.save(shop);
        return getLinkInfo(ownerId);
    }
}
