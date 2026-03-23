package com.example.easybooking.shop.presentation;

import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.shop.dto.request.AddTagToMenuRequest;
import com.example.easybooking.shop.dto.request.CreateShopTagRequest;
import com.example.easybooking.shop.dto.request.CreateTagRequest;
import com.example.easybooking.shop.dto.response.TagResponse;
import com.example.easybooking.shop.service.TagService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping("/api/tags")
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createOrGet(request.getName()));
    }

    @PostMapping("/api/shops/{shopId}/tags")
    public ResponseEntity<TagResponse> createShopTag(
            @PathVariable Long shopId,
            @Valid @RequestBody CreateShopTagRequest request,
            @RequireAuthenticatedUser AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tagService.createShopTag(shopId, user.getUserId(), request.getName()));
    }

    @GetMapping("/api/tags")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/api/shops/{shopId}/tags")
    public ResponseEntity<List<TagResponse>> getVisibleShopTags(@PathVariable Long shopId) {
        return ResponseEntity.ok(tagService.getVisibleShopTags(shopId));
    }

    @PostMapping("/api/shops/{shopId}/menus/{menuId}/tags")
    public ResponseEntity<Void> addTagToMenu(
            @PathVariable Long shopId,
            @PathVariable Long menuId,
            @Valid @RequestBody AddTagToMenuRequest request) {
        tagService.addTagToMenu(menuId, request.getTagId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/shops/{shopId}/menus/{menuId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromMenu(
            @PathVariable Long shopId,
            @PathVariable Long menuId,
            @PathVariable Long tagId) {
        tagService.removeTagFromMenu(menuId, tagId);
        return ResponseEntity.ok().build();
    }
}
