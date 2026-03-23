package com.example.easybooking.shop;

import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopMenuReader {

    private final ShopMenuRepository shopMenuRepository;

    public List<ShopMenu> findActiveByShopId(Long shopId) {
        return shopMenuRepository.findByShopIdAndIsActiveTrueOrderBySortOrderAsc(shopId);
    }

    public ShopMenu getById(Long id) {
        return shopMenuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ShopMenu not found: " + id));
    }

    public ShopMenu getByIdAndShopId(Long shopId, Long id) {
        return shopMenuRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new RuntimeException("ShopMenu not found: " + id));
    }

    public List<ShopMenu> findActiveByShopIdAndTagIds(Long shopId, List<Long> tagIds) {
        return shopMenuRepository.findActiveByShopIdAndTagIds(shopId, tagIds);
    }
}
