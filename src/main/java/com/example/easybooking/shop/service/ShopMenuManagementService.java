package com.example.easybooking.shop.service;

import com.example.easybooking.shop.ShopMenuReader;
import com.example.easybooking.shop.ShopMenuWriter;
import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.dto.request.CreateShopMenuRequest;
import com.example.easybooking.shop.dto.request.UpdateShopMenuRequest;
import com.example.easybooking.shop.dto.response.MenuTagRow;
import com.example.easybooking.shop.dto.response.ShopMenuResponse;
import com.example.easybooking.shop.dto.response.TagResponse;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopMenuManagementService {

    private final ShopMenuReader shopMenuReader;
    private final ShopMenuWriter shopMenuWriter;
    private final ShopMenuTagRepository shopMenuTagRepository;

    @Transactional
    public ShopMenuResponse create(Long shopId, CreateShopMenuRequest request) {
        ShopMenu menu = ShopMenu.create(shopId, request.getName(), request.getDescription(),
                true, request.getSortOrder());
        ShopMenu saved = shopMenuWriter.save(menu);
        return toResponse(shopId, saved);
    }

    public List<ShopMenuResponse> getActiveMenus(Long shopId, List<Long> tagIds) {
        List<ShopMenu> menus;
        if (tagIds == null || tagIds.isEmpty()) {
            menus = shopMenuReader.findActiveByShopId(shopId);
        } else {
            menus = shopMenuReader.findActiveByShopIdAndTagIds(shopId, tagIds);
        }
        return toResponses(shopId, menus);
    }

    @Transactional
    public ShopMenuResponse update(Long shopId, Long menuId, UpdateShopMenuRequest request) {
        ShopMenu menu = shopMenuReader.getById(menuId);
        menu.update(request.getName(), request.getDescription(), request.getSortOrder());
        return toResponse(shopId, menu);
    }

    @Transactional
    public void deactivate(Long shopId, Long menuId) {
        ShopMenu menu = shopMenuReader.getById(menuId);
        menu.deactivate();
    }

    private ShopMenuResponse toResponse(Long shopId, ShopMenu menu) {
        return toResponses(shopId, List.of(menu)).get(0);
    }

    private List<ShopMenuResponse> toResponses(Long shopId, List<ShopMenu> menus) {
        if (menus.isEmpty()) {
            return List.of();
        }

        List<Long> menuIds = menus.stream()
                .map(ShopMenu::getId)
                .toList();
        Map<Long, List<TagResponse>> tagsByMenuId = shopMenuTagRepository.findShopTagsByMenuIds(shopId, menuIds)
                .stream()
                .collect(Collectors.groupingBy(
                        MenuTagRow::menuId,
                        Collectors.mapping(
                                row -> new TagResponse(row.tagId(), row.tagName()),
                                Collectors.toList()
                        )
                ));

        return menus.stream()
                .map(menu -> new ShopMenuResponse(menu, tagsByMenuId.getOrDefault(menu.getId(), List.of())))
                .toList();
    }
}
