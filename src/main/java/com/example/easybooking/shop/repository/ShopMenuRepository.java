package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopMenu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopMenuRepository extends JpaRepository<ShopMenu, Long> {

    List<ShopMenu> findByShopIdAndIsActiveTrueOrderBySortOrderAsc(Long shopId);

    @Query("SELECT DISTINCT m FROM ShopMenu m JOIN ShopMenuTag smt ON m.id = smt.shopMenuId "
            + "WHERE m.shopId = :shopId AND m.isActive = true AND smt.tagId IN :tagIds "
            + "ORDER BY m.sortOrder ASC")
    List<ShopMenu> findActiveByShopIdAndTagIds(@Param("shopId") Long shopId,
                                               @Param("tagIds") List<Long> tagIds);
}
