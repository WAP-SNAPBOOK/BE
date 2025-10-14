package com.example.easybooking.shop;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ShopReader {
    private final ShopRepository shopRepository;

    public Shop read(Long id){
        return shopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid shop ID"));
    }
}
