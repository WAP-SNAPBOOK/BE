package com.example.easybooking.shop.service;

import com.example.easybooking.errors.errorcode.ShopErrorCode;
import com.example.easybooking.errors.exception.ShopException;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.ShopMenuTag;
import com.example.easybooking.shop.domain.ShopTag;
import com.example.easybooking.shop.domain.Tag;
import com.example.easybooking.shop.dto.response.TagResponse;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import com.example.easybooking.shop.repository.ShopTagRepository;
import com.example.easybooking.shop.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final ShopReader shopReader;
    private final ShopTagRepository shopTagRepository;
    private final ShopMenuTagRepository shopMenuTagRepository;

    @Transactional
    public TagResponse createOrGet(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(Tag.create(name)));
        return new TagResponse(tag);
    }

    @Transactional
    public TagResponse createShopTag(Long shopId, String name) {
        return shopTagRepository.findByShopIdAndName(shopId, name)
                .map(TagResponse::new)
                .orElseGet(() -> {
                    int nextSortOrder = shopTagRepository.findMaxSortOrderByShopId(shopId) + 1;
                    ShopTag tag = shopTagRepository.save(ShopTag.create(shopId, name, nextSortOrder));
                    return new TagResponse(tag);
                });
    }

    @Transactional
    public TagResponse createShopTag(Long shopId, Long ownerUserId, String name) {
        validateOwner(shopId, ownerUserId);
        return createShopTag(shopId, name);
    }

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(TagResponse::new)
                .toList();
    }

    public List<TagResponse> getVisibleShopTags(Long shopId) {
        return shopTagRepository.findVisibleByShopIdOrderBySortOrderAsc(shopId).stream()
                .map(TagResponse::new)
                .toList();
    }

    @Transactional
    public void addTagToMenu(Long menuId, Long tagId) {
        shopMenuTagRepository.save(ShopMenuTag.create(menuId, tagId));
    }

    @Transactional
    public void removeTagFromMenu(Long menuId, Long tagId) {
        shopMenuTagRepository.deleteByShopMenuIdAndTagId(menuId, tagId);
    }

    private void validateOwner(Long shopId, Long ownerUserId) {
        if (!shopReader.isShopOwnedBy(shopId, ownerUserId)) {
            throw new ShopException(ShopErrorCode.SHOP_OWNER_MISMATCH);
        }
    }
}
