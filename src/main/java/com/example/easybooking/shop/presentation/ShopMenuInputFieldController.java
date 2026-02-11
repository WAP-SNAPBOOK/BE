package com.example.easybooking.shop.presentation;

import com.example.easybooking.shop.dto.request.CreateInputFieldRequest;
import com.example.easybooking.shop.dto.request.UpdateInputFieldRequest;
import com.example.easybooking.shop.dto.response.InputFieldResponse;
import com.example.easybooking.shop.service.ShopMenuInputFieldService;
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
@RequestMapping("/api/shops/{shopId}/menus/{menuId}/input-fields")
public class ShopMenuInputFieldController {

    private final ShopMenuInputFieldService inputFieldService;

    @PostMapping
    public ResponseEntity<InputFieldResponse> create(
            @PathVariable Long shopId,
            @PathVariable Long menuId,
            @Valid @RequestBody CreateInputFieldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inputFieldService.create(menuId, request));
    }

    @GetMapping
    public ResponseEntity<List<InputFieldResponse>> getFields(
            @PathVariable Long shopId,
            @PathVariable Long menuId) {
        return ResponseEntity.ok(inputFieldService.getActiveFields(menuId));
    }

    @PatchMapping("/{fieldId}")
    public ResponseEntity<InputFieldResponse> update(
            @PathVariable Long shopId,
            @PathVariable Long menuId,
            @PathVariable Long fieldId,
            @RequestBody UpdateInputFieldRequest request) {
        return ResponseEntity.ok(inputFieldService.update(fieldId, request));
    }

    @DeleteMapping("/{fieldId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long shopId,
            @PathVariable Long menuId,
            @PathVariable Long fieldId) {
        inputFieldService.deactivate(fieldId);
        return ResponseEntity.ok().build();
    }
}
