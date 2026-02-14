package com.example.easybooking.availability.repository;

import com.example.easybooking.availability.domain.ShopHoliday;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopHolidayRepository extends JpaRepository<ShopHoliday, Long> {
    List<ShopHoliday> findByShopId(Long shopId);

    Optional<ShopHoliday> findByIdAndShopId(Long id, Long shopId);
}
