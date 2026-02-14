package com.example.easybooking.availability.repository;

import com.example.easybooking.availability.domain.ShopSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopSettingsRepository extends JpaRepository<ShopSettings, Long> {
    Optional<ShopSettings> findByShopId(Long shopId);
}
