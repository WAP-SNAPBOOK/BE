package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ShopOperatingTimeWriter {

    private final ShopOperatingTimeRepository shopOperatingTimeRepository;

    public ShopOperatingTimeWriter(ShopOperatingTimeRepository shopOperatingTimeRepository) {
        this.shopOperatingTimeRepository = shopOperatingTimeRepository;
    }

    public List<ShopOperatingTime> replaceAll(Long shopId, List<ShopOperatingTime> shopOperatingTimes) {
        shopOperatingTimeRepository.deleteByShopId(shopId);
        return shopOperatingTimeRepository.saveAllAndFlush(shopOperatingTimes);
    }
}
