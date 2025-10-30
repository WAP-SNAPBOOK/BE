package com.example.easybooking.shop.presentation;

import com.example.easybooking.auth.AuthenticatedUser;
import com.example.easybooking.auth.RequireAuthenticatedUser;
import com.example.easybooking.shop.dto.CreateShopRequest;
import com.example.easybooking.shop.dto.CreateShopResponse;
import com.example.easybooking.shop.dto.LinkInfoResponse;
import com.example.easybooking.shop.dto.SlugUpdateRequest;
import com.example.easybooking.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/link")
    public ResponseEntity<LinkInfoResponse> getLink(
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(shopService.getLinkInfo(user.getUserId()));
    }

    @PutMapping("/link/slug")
    public ResponseEntity<LinkInfoResponse> updateSlug(
            @RequestBody @Valid SlugUpdateRequest request,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(shopService.updateSlug(user.getUserId(), request));
    }
}
