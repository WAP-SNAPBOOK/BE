package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.ShopMenuTag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopMenuTagRepository extends JpaRepository<ShopMenuTag, Long> {

    Optional<ShopMenuTag> findByShopMenuIdAndTagId(Long shopMenuId, Long tagId);

    Optional<ShopMenuTag> findByShopMenuIdAndShopTagId(Long shopMenuId, Long shopTagId);
}
