package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopOperatingTimeReader {

    private final ShopOperatingTimeRepository shopOperatingTimeRepository;

    public List<ShopOperatingTime> readByShopIdAndDayOfWeek(Long shopId, DayOfWeek dayOfWeek) {
        return shopOperatingTimeRepository.findByShopIdAndDayOfWeek(shopId, dayOfWeek);
    }
}
