package com.example.easybooking.shop;

import com.example.easybooking.errors.errorcode.ShopErrorCode;
import com.example.easybooking.errors.exception.ShopException;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.repository.ShopRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShopReader {
    private final ShopRepository shopRepository;

    public boolean isExist(Long ownerId) {
        return shopRepository.existsByOwnerId(ownerId);
    }

    public Shop read(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new ShopException(ShopErrorCode.SHOP_NOT_FOUND));
    }

    public Shop readByPublicCode(String code) {
        return shopRepository.findByPublicCode(code)
                .orElseThrow(() -> new ShopException(ShopErrorCode.INVALID_LINK_CODE));
    }

    public Shop readBySlug(String slug) {
        return shopRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Shop with slug '" + slug + "' not found."));
    }

    public Shop readByOwnerId(Long ownerId) {
        return shopRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ShopException(ShopErrorCode.SHOP_NOT_FOUND_FOR_OWNER));
    }

    public List<Long> findShopIdsByOwnerId(Long ownerId) {
        return shopRepository.findAllByOwnerId(ownerId).stream()
                .map(Shop::getId)
                .collect(Collectors.toList());
    }

    public boolean isShopOwnedBy(Long shopId, Long ownerUserId) {
        return shopRepository.existsByIdAndOwnerId(shopId, ownerUserId);
    }

    public List<Shop> readAllByIds(List<Long> shopIds) {
        return shopRepository.findAllById(shopIds);
    }
}
