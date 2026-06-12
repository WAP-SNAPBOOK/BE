package com.example.easybooking.shop.service;

import com.example.easybooking.availability.ShopSettingsWriter;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.ShopWriter;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.dto.request.SlugUpdateRequest;
import com.example.easybooking.shop.dto.response.CreateShopResponse;
import com.example.easybooking.shop.dto.response.LinkInfoResponse;
import com.example.easybooking.shop.dto.response.ShopInfoResponse;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.staff.StaffWriter;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.user.UserReader;
import java.util.List;
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
    private static final Logger log = LoggerFactory.getLogger(ShopService.class);
    private final ShopReader shopReader;
    private final ShopRepository shopRepository;
    private final StaffReader staffReader;
    private final StaffWriter staffWriter;
    private final UserReader userReader;
    private final ShopSettingsWriter shopSettingsWriter;

    @Value("${server-url:https://snapbook.store}")
    private String serverUrl;

    @Transactional
    public CreateShopResponse createShop(Long ownerId, CreateShopRequest request) {
        CreateShopResponse response = shopwriter.create(ownerId, request);
        Long shopId = response.getShopId();

        // Shop 생성 트랜잭션 내에서 기본 Staff(Owner) 생성
        List<Staff> staffs = staffReader.findByShopId(shopId);
        if (staffs.isEmpty()) {
            staffWriter.save(Staff.create(shopId, userReader.read(ownerId).getName()));
            log.info("Shop ID: {} - 기본 Staff 생성 완료 및 할당", shopId);
        }

        // Shop 생성 트랜잭션 내에서 ShopSettings 기본값 보장
        shopSettingsWriter.ensureDefaultByShopId(shopId);
        log.info("Shop ID: {} - 기본 설정 생성 완료 및 할당", shopId);

        return response;
    }

    public ShopInfoResponse getShopInfo(String slugOrCode) {
        Shop shop = shopReader.readBySlugOrPublicCode(slugOrCode);
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
        return LinkInfoResponse.of(shop.getId(), fullUrl, "/s/" + slugOrCode, shop.getSlug(), shop.getPublicCode());
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
