package com.example.easybooking.shop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateShopMenuRequest {

    private String name;

    private String description;

    private Integer sortOrder;

    public UpdateShopMenuRequest(String name, String description, Integer sortOrder) {
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
    }
}
