package com.example.easybooking.shop;

import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.repository.ShopRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

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
}
