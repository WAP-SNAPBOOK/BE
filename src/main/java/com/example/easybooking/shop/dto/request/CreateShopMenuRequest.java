package com.example.easybooking.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateShopMenuRequest {

    @NotBlank
    private String name;

    private String description;

    private Long price;

    private int sortOrder;

    public CreateShopMenuRequest(String name, String description, int sortOrder) {
        this(name, description, null, sortOrder);
    }

    public CreateShopMenuRequest(String name, String description, Long price, int sortOrder) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.sortOrder = sortOrder;
    }
}
