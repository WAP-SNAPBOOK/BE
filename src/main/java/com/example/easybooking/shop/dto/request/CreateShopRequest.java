package com.example.easybooking.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
public class CreateShopRequest {
    @NotBlank(message = "상호명은 필수입니다")
    private String businessName;
    private String address;
    private String businessNumber;
}
