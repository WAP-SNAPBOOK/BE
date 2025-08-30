package com.example.easybooking.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShopReader {
    private ShopRepository shopRepository;

    public Shop read(Long id){
        return shopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid shop ID"));
    }
}
