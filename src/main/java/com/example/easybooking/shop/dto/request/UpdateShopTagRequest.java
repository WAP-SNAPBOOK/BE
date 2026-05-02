package com.example.easybooking.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateShopTagRequest {

    @NotBlank
    private String name;

    public UpdateShopTagRequest(String name) {
        this.name = name;
    }
}
