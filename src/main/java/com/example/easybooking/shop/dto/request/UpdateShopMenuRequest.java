package com.example.easybooking.shop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateShopMenuRequest {

    private String name;

    private String description;

    private Long price;

    private Integer sortOrder;

    public UpdateShopMenuRequest(String name, String description, Integer sortOrder) {
        this(name, description, null, sortOrder);
    }

    public UpdateShopMenuRequest(String name, String description, Long price, Integer sortOrder) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.sortOrder = sortOrder;
    }
}
