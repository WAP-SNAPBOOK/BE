package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopTagRepository extends JpaRepository<ShopTag, Long> {

    Optional<ShopTag> findByShopIdAndName(Long shopId, String name);

    List<ShopTag> findByShopIdOrderBySortOrderAsc(Long shopId);

    @Query("select coalesce(max(st.sortOrder), -1) from ShopTag st where st.shopId = :shopId")
    int findMaxSortOrderByShopId(Long shopId);

    @Query("select distinct st from ShopTag st "
            + "join ShopMenuTag smt on smt.shopTagId = st.id "
            + "join ShopMenu m on m.id = smt.shopMenuId "
            + "where st.shopId = :shopId and m.isActive = true "
            + "order by st.sortOrder asc")
    List<ShopTag> findVisibleByShopTagIdOrderBySortOrderAsc(@Param("shopId") Long shopId);

    @Query("select distinct st from ShopTag st "
            + "join ShopMenu m on m.shopId = st.shopId "
            + "join ShopMenuTag smt on smt.shopMenuId = m.id "
            + "join Tag t on t.id = smt.tagId "
            + "where st.shopId = :shopId and m.isActive = true "
            + "and smt.shopTagId is null and t.name = st.name "
            + "order by st.sortOrder asc")
    List<ShopTag> findVisibleByLegacyTagFallbackOrderBySortOrderAsc(@Param("shopId") Long shopId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ShopTag st set st.sortOrder = st.sortOrder + :offset where st.shopId = :shopId")
    void shiftSortOrders(@Param("shopId") Long shopId, @Param("offset") int offset);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ShopTag st set st.sortOrder = :sortOrder where st.shopId = :shopId and st.id = :tagId")
    void updateSortOrder(@Param("shopId") Long shopId,
                         @Param("tagId") Long tagId,
                         @Param("sortOrder") int sortOrder);
}
