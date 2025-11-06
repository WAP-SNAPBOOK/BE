package com.example.easybooking.shop.dto.response;

import com.example.easybooking.shop.domain.Shop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class CreateShopResponse {
    private Long ownerId;
    private Long shopId;
    private String businessName;
    private String address;
    private String businessNumber;

    public static CreateShopResponse of(Long ownerId,Shop shop){
        return new CreateShopResponse(
                ownerId,
                shop.getId(),
                shop.getBusinessName(),
                shop.getAddress(),
                shop.getBusinessNumber()
        );
    }
}
