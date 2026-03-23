package com.example.easybooking.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateShopTagRequest {

    @NotBlank
    private String name;

    public CreateShopTagRequest(String name) {
        this.name = name;
    }
}
