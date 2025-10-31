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

    public List<Long> findShopIdsByOwnerId(Long ownerId) {
        return shopRepository.findAllByOwnerId(ownerId).stream()
                .map(Shop::getId)
                .collect(Collectors.toList());
    }

    public boolean isShopOwnedBy(Long shopId, Long ownerUserId) {
        return shopRepository.existsByIdAndOwnerId(shopId, ownerUserId);
    }
}
