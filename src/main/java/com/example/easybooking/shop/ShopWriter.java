package com.example.easybooking.shop;

import com.example.easybooking.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopWriter {
    private final ShopRepository shopRepository;

}
