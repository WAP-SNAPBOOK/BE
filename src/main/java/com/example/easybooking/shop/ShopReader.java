package com.example.easybooking.shop;

import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.repository.ShopRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ShopReader {
    private final ShopRepository shopRepository;

    public boolean isExist(Long ownerId) {
        return shopRepository.existsByOwnerId(ownerId);
    }

    public Shop read(Long id){
        return shopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid shop ID"));
    }

    public Shop readByPublicCode(String code) {
        return shopRepository.findByPublicCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Invalid link code"));
    }

    public Shop readBySlug(String slug) {
        return shopRepository.findBySlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Invalid slug"));
    }

    public Shop readByOwnerId(Long ownerId) {
        return shopRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found for owner"));
    }

    public List<Long> findShopIdsByOwnerId(Long ownerId) {
        return shopRepository.findAllByOwnerId(ownerId).stream()
                .map(Shop::getId)
                .collect(Collectors.toList());
    }

    public boolean isShopOwnedBy(Long shopId, Long ownerUserId) {
        return shopRepository.existsByIdAndOwnerId(shopId, ownerUserId);
    }
}
