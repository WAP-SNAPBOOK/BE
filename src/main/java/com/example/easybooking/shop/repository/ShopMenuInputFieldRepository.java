package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopMenuInputField;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopMenuInputFieldRepository extends JpaRepository<ShopMenuInputField, Long> {

    List<ShopMenuInputField> findByShopMenuIdAndIsActiveTrueOrderBySortOrderAsc(Long shopMenuId);

    void deleteByShopMenuIdIn(List<Long> shopMenuIds);
}
