package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopMenuTag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopMenuTagRepository extends JpaRepository<ShopMenuTag, Long> {

    Optional<ShopMenuTag> findByShopMenuIdAndTagId(Long shopMenuId, Long tagId);

    Optional<ShopMenuTag> findByShopMenuIdAndShopTagId(Long shopMenuId, Long shopTagId);

    void deleteByShopMenuIdIn(java.util.List<Long> shopMenuIds);

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
