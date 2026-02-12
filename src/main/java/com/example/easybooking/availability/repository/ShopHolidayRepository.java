package com.example.easybooking.availability.repository;

import com.example.easybooking.availability.domain.ShopHoliday;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopHolidayRepository extends JpaRepository<ShopHoliday, Long> {
    List<ShopHoliday> findByShopId(Long shopId);
}
