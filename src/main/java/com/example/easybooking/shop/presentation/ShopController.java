package com.example.easybooking.shop.presentation;

import com.example.easybooking.auth.AuthenticatedUser;
import com.example.easybooking.auth.RequireAuthenticatedUser;
import com.example.easybooking.shop.dto.CreateShopRequest;
import com.example.easybooking.shop.dto.CreateShopResponse;
import com.example.easybooking.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop")
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<CreateShopResponse>  createShop(
            @Valid @RequestBody CreateShopRequest request,
            @RequireAuthenticatedUser AuthenticatedUser user){
        CreateShopResponse createShopResponse = shopService.createShop(user.getUserId(), request);
        return ResponseEntity.ok(createShopResponse);
    }
}
