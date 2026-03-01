package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopSettingsWriter {

    private final ShopSettingsRepository shopSettingsRepository;

    public ShopSettings save(ShopSettings shopSettings) {
        return shopSettingsRepository.save(shopSettings);
    }

    public ShopSettings ensureDefaultByShopId(Long shopId) {
        return shopSettingsRepository.findByShopId(shopId)
                .orElseGet(() -> shopSettingsRepository.save(ShopSettings.createDefault(shopId)));
    }
}
