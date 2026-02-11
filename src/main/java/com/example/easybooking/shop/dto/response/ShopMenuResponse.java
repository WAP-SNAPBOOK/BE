package com.example.easybooking.shop.dto.response;

import com.example.easybooking.shop.domain.ShopMenu;
import lombok.Getter;

@Getter
public class ShopMenuResponse {

    private final Long id;
    private final Long shopId;
    private final String name;
    private final String description;
    private final Boolean isActive;
    private final Integer sortOrder;

    public ShopMenuResponse(ShopMenu menu) {
        this.id = menu.getId();
        this.shopId = menu.getShopId();
        this.name = menu.getName();
        this.description = menu.getDescription();
        this.isActive = menu.getIsActive();
        this.sortOrder = menu.getSortOrder();
    }
}
