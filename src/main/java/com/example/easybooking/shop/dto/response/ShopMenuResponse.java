package com.example.easybooking.shop.dto.response;

import com.example.easybooking.shop.domain.ShopMenu;
import java.util.List;
import lombok.Getter;

@Getter
public class ShopMenuResponse {

    private final Long id;
    private final Long shopId;
    private final String name;
    private final String description;
    private final Long price;
    private final Boolean isActive;
    private final Integer sortOrder;
    private final List<TagResponse> tags;

    public ShopMenuResponse(ShopMenu menu) {
        this(menu, List.of());
    }

    public ShopMenuResponse(ShopMenu menu, List<TagResponse> tags) {
        this.id = menu.getId();
        this.shopId = menu.getShopId();
        this.name = menu.getName();
        this.description = menu.getDescription();
        this.price = menu.getPrice();
        this.isActive = menu.getIsActive();
        this.sortOrder = menu.getSortOrder();
        this.tags = List.copyOf(tags);
    }
}
