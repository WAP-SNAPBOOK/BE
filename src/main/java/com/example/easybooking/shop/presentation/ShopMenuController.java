package com.example.easybooking.shop.presentation;

import com.example.easybooking.shop.dto.request.CreateShopMenuRequest;
import com.example.easybooking.shop.dto.request.UpdateShopMenuRequest;
import com.example.easybooking.shop.dto.response.ShopMenuResponse;
import com.example.easybooking.shop.service.ShopMenuManagementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops/{shopId}/menus")
public class ShopMenuController {

    private final ShopMenuManagementService shopMenuManagementService;

    @PostMapping
    public ResponseEntity<ShopMenuResponse> create(
            @PathVariable Long shopId,
            @Valid @RequestBody CreateShopMenuRequest request) {
        ShopMenuResponse response = shopMenuManagementService.create(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ShopMenuResponse>> getMenus(@PathVariable Long shopId) {
        return ResponseEntity.ok(shopMenuManagementService.getActiveMenus(shopId));
    }

    @PatchMapping("/{menuId}")
    public ResponseEntity<ShopMenuResponse> update(
            @PathVariable Long shopId,
            @PathVariable Long menuId,
            @RequestBody UpdateShopMenuRequest request) {
        return ResponseEntity.ok(shopMenuManagementService.update(shopId, menuId, request));
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long shopId,
            @PathVariable Long menuId) {
        shopMenuManagementService.deactivate(shopId, menuId);
        return ResponseEntity.ok().build();
    }
}
