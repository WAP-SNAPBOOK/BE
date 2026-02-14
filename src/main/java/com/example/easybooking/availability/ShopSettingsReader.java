package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.exception.ShopSettingsNotFoundException;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopSettingsReader {

    private final ShopSettingsRepository shopSettingsRepository;

    public ShopSettings readByShopId(Long shopId) {
        return shopSettingsRepository.findByShopId(shopId)
                .orElseThrow(ShopSettingsNotFoundException::new);
    }
}
