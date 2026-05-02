package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopMenuTag;
import com.example.easybooking.shop.dto.response.MenuTagRow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopMenuTagRepository extends JpaRepository<ShopMenuTag, Long> {

    Optional<ShopMenuTag> findByShopMenuIdAndTagId(Long shopMenuId, Long tagId);

    Optional<ShopMenuTag> findByShopMenuIdAndShopTagId(Long shopMenuId, Long shopTagId);

    void deleteByShopMenuIdIn(java.util.List<Long> shopMenuIds);

    @Query("select new com.example.easybooking.shop.dto.response.MenuTagRow("
            + "smt.shopMenuId, st.id, st.name, st.sortOrder) "
            + "from ShopMenuTag smt "
            + "join ShopTag st on st.id = smt.shopTagId "
            + "where smt.shopMenuId in :menuIds and st.shopId = :shopId "
            + "order by smt.shopMenuId asc, st.sortOrder asc, st.id asc")
    List<MenuTagRow> findShopTagsByMenuIds(@Param("shopId") Long shopId,
                                           @Param("menuIds") List<Long> menuIds);

    @Query("select smt from ShopMenuTag smt where smt.shopMenuId = :shopMenuId "
            + "and (smt.shopTagId = :tagId or smt.tagId = :tagId)")
    Optional<ShopMenuTag> findByShopMenuIdAndAnyTagId(@Param("shopMenuId") Long shopMenuId,
                                                      @Param("tagId") Long tagId);

    @Modifying
    @Query("delete from ShopMenuTag smt where smt.shopMenuId = :shopMenuId "
            + "and (smt.shopTagId = :tagId or smt.tagId = :tagId)")
    void deleteByShopMenuIdAndAnyTagId(@Param("shopMenuId") Long shopMenuId,
                                       @Param("tagId") Long tagId);

    @Modifying
    @Query("delete from ShopMenuTag smt where smt.shopTagId = :shopTagId")
    void deleteByShopTagId(@Param("shopTagId") Long shopTagId);
}
