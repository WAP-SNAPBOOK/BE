package com.example.easybooking.shop;

import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopMenuWriter {

    private final ShopMenuRepository shopMenuRepository;

    public ShopMenu save(ShopMenu shopMenu) {
        return shopMenuRepository.save(shopMenu);
    }
}
