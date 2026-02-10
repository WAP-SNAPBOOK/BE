package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopMenu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopMenuRepository extends JpaRepository<ShopMenu, Long> {

    List<ShopMenu> findByShopIdAndIsActiveTrueOrderBySortOrderAsc(Long shopId);
}
