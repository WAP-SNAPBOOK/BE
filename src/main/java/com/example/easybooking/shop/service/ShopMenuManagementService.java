package com.example.easybooking.shop.service;

import com.example.easybooking.shop.ShopMenuReader;
import com.example.easybooking.shop.ShopMenuWriter;
import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.dto.request.CreateShopMenuRequest;
import com.example.easybooking.shop.dto.request.UpdateShopMenuRequest;
import com.example.easybooking.shop.dto.response.ShopMenuResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopMenuManagementService {

    private final ShopMenuReader shopMenuReader;
    private final ShopMenuWriter shopMenuWriter;

    @Transactional
    public ShopMenuResponse create(Long shopId, CreateShopMenuRequest request) {
        ShopMenu menu = ShopMenu.create(shopId, request.getName(), request.getDescription(),
                true, request.getSortOrder());
        ShopMenu saved = shopMenuWriter.save(menu);
        return new ShopMenuResponse(saved);
    }

    public List<ShopMenuResponse> getActiveMenus(Long shopId, List<Long> tagIds) {
        List<ShopMenu> menus;
        if (tagIds == null || tagIds.isEmpty()) {
            menus = shopMenuReader.findActiveByShopId(shopId);
        } else {
            menus = shopMenuReader.findActiveByShopIdAndTagIds(shopId, tagIds);
        }
        return menus.stream()
                .map(ShopMenuResponse::new)
                .toList();
    }

    @Transactional
    public ShopMenuResponse update(Long shopId, Long menuId, UpdateShopMenuRequest request) {
        ShopMenu menu = shopMenuReader.getById(menuId);
        menu.update(request.getName(), request.getDescription(), request.getSortOrder());
        return new ShopMenuResponse(menu);
    }

    @Transactional
    public void deactivate(Long shopId, Long menuId) {
        ShopMenu menu = shopMenuReader.getById(menuId);
        menu.deactivate();
    }
}
