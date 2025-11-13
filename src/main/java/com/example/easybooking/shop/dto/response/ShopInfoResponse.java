package com.example.easybooking.shop.dto.response;

import com.example.easybooking.shop.domain.Shop;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ShopInfoResponse {
    private Long shopId;
    private String shopName;

    public  ShopInfoResponse(Shop shop) {
        this.shopId = shop.getId();
        this.shopName = shop.getBusinessName();
    }
}
