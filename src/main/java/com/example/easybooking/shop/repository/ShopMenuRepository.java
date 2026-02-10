package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopMenu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopMenuRepository extends JpaRepository<ShopMenu, Long> {
}
