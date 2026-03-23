package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShopTagRepository extends JpaRepository<ShopTag, Long> {

    Optional<ShopTag> findByShopIdAndName(Long shopId, String name);

    List<ShopTag> findByShopIdOrderBySortOrderAsc(Long shopId);

    @Query("select coalesce(max(st.sortOrder), -1) from ShopTag st where st.shopId = :shopId")
    int findMaxSortOrderByShopId(Long shopId);
}
