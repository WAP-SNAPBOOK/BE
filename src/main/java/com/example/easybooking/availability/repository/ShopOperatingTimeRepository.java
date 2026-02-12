package com.example.easybooking.availability.repository;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopOperatingTimeRepository extends JpaRepository<ShopOperatingTime, Long> {
    List<ShopOperatingTime> findByShopId(Long shopId);

    List<ShopOperatingTime> findByShopIdAndDayOfWeek(Long shopId, DayOfWeek dayOfWeek);

    void deleteByShopId(Long shopId);
}
