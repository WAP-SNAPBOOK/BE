package com.example.easybooking.shop.presentation;

import com.example.easybooking.auth.AuthenticatedUser;
import com.example.easybooking.auth.RequireAuthenticatedUser;
import com.example.easybooking.shop.dto.CreateShopRequest;
import com.example.easybooking.shop.dto.CreateShopResponse;
import com.example.easybooking.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    public ResponseEntity<CreateShopResponse>  createShop(
            CreateShopRequest request,
            @RequireAuthenticatedUser AuthenticatedUser user){
        CreateShopResponse createShopResponse = shopService.createShop(user.getUserId(), request);
        return ResponseEntity.ok(createShopResponse);
    }
}
