package com.example.easybooking.shop.service;

import com.example.easybooking.form.FormService;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.ShopWriter;
import com.example.easybooking.shop.dto.CreateShopRequest;
import com.example.easybooking.shop.dto.CreateShopResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopWriter shopwriter;
    private final FormService formService;
    private static final Logger log = LoggerFactory.getLogger(ShopService.class);

    @Transactional
    public CreateShopResponse createShop(Long ownerId, CreateShopRequest request) {
        CreateShopResponse response = shopwriter.create(ownerId, request);
        Long shopId = response.getShopId();

        // Shop 생성 트랜잭션 내에서 Form 엔티티 생성
        formService.createDefaultForm(shopId);
        log.info("Shop ID: {} - 기본 폼 생성 완료 및 할당", shopId);

        return response;
    }
}
