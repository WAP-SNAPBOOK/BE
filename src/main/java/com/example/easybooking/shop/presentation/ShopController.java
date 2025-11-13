package com.example.easybooking.shop.presentation;



import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.dto.response.CreateShopResponse;
import com.example.easybooking.shop.dto.response.LinkInfoResponse;
import com.example.easybooking.shop.dto.request.SlugUpdateRequest;
import com.example.easybooking.shop.dto.response.ShopInfoResponse;

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

    @GetMapping("/{slugOrCode}")
    public ResponseEntity<ShopInfoResponse> getShop(
            @PathVariable String slugOrCode,
            @RequireAuthenticatedUser AuthenticatedUser user){
        ShopInfoResponse response = shopService.getShopInfo(slugOrCode);
        return ResponseEntity.ok(response);
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
