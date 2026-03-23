package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopTagRepository extends JpaRepository<ShopTag, Long> {

    Optional<ShopTag> findByShopIdAndName(Long shopId, String name);

    List<ShopTag> findByShopIdOrderBySortOrderAsc(Long shopId);

    @Query("select coalesce(max(st.sortOrder), -1) from ShopTag st where st.shopId = :shopId")
    int findMaxSortOrderByShopId(Long shopId);

    @Query("select distinct st from ShopTag st "
            + "join ShopMenu m on m.shopId = st.shopId "
            + "join ShopMenuTag smt on smt.shopMenuId = m.id "
            + "left join Tag t on t.id = smt.tagId "
            + "where st.shopId = :shopId and m.isActive = true "
            + "and (smt.shopTagId = st.id or (smt.shopTagId is null and t.name = st.name)) "
            + "order by st.sortOrder asc")
    List<ShopTag> findVisibleByShopIdOrderBySortOrderAsc(@Param("shopId") Long shopId);
}
