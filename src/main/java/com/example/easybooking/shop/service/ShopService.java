package com.example.easybooking.shop.service;

import com.example.easybooking.form.FormService;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.ShopWriter;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.dto.request.SlugUpdateRequest;
import com.example.easybooking.shop.dto.response.CreateShopResponse;
import com.example.easybooking.shop.dto.response.LinkInfoResponse;
import com.example.easybooking.shop.dto.response.ShopInfoResponse;
import com.example.easybooking.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopWriter shopwriter;
    private final FormService formService;
    private static final Logger log = LoggerFactory.getLogger(ShopService.class);
    private final ShopReader shopReader;
    private final ShopRepository shopRepository;

    @Value("${server-url:https://snapbook.store}")
    private String serverUrl;

    @Transactional
    public CreateShopResponse createShop(Long ownerId, CreateShopRequest request) {
        CreateShopResponse response = shopwriter.create(ownerId, request);
        Long shopId = response.getShopId();

        // Shop 생성 트랜잭션 내에서 Form 엔티티 생성
        formService.createDefaultForm(shopId);
        log.info("Shop ID: {} - 기본 폼 생성 완료 및 할당", shopId);

        return response;
    }

    public ShopInfoResponse getShopInfo(String slugOrCode) {
        Shop shop;
        try {
            shop = shopReader.readBySlug(slugOrCode);
        } catch (IllegalArgumentException ignore) {
            shop = shopReader.readByPublicCode(slugOrCode);
        }
        return new ShopInfoResponse(shop);
    }

    public ShopInfoResponse getShopInfo(Long shopId) {
        Shop shop = shopReader.read(shopId);
        return new ShopInfoResponse(shop);
    }

    @Transactional(readOnly = true)
    public LinkInfoResponse getLinkInfo(Long ownerId) {
        Shop shop = shopReader.readByOwnerId(ownerId);
        String slugOrCode = (shop.getSlug() != null && !shop.getSlug().isBlank())
                ? shop.getSlug() : shop.getPublicCode();
        String canonical = "/s/" + slugOrCode;
        String fullUrl = serverUrl + canonical;
        return LinkInfoResponse.of(fullUrl, "/s/" + slugOrCode, shop.getSlug(), shop.getPublicCode());
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
