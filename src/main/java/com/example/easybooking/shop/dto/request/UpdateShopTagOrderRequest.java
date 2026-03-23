package com.example.easybooking.shop.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateShopTagOrderRequest {

    @NotEmpty
    private List<Long> tagIds;

    public UpdateShopTagOrderRequest(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
}
