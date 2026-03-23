package com.example.easybooking.shop.service;

import com.example.easybooking.errors.errorcode.ShopErrorCode;
import com.example.easybooking.errors.exception.ShopException;
import com.example.easybooking.shop.ShopMenuReader;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.ShopMenu;
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
    private final ShopMenuReader shopMenuReader;
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
    public void addTagToMenu(Long shopId, Long menuId, Long tagId) {
        ShopMenu menu = readMenuInShop(shopId, menuId);
        ResolvedTagIds resolvedTagIds = resolveTagIds(shopId, tagId);

        if (shopMenuTagRepository.findByShopMenuIdAndShopTagId(menu.getId(), resolvedTagIds.shopTagId()).isPresent()) {
            return;
        }

        shopMenuTagRepository.save(ShopMenuTag.createResolved(
                menu.getId(),
                resolvedTagIds.legacyTagId(),
                resolvedTagIds.shopTagId()
        ));
    }

    @Transactional
    public void removeTagFromMenu(Long shopId, Long menuId, Long tagId) {
        readMenuInShop(shopId, menuId);

        shopTagRepository.findById(tagId).ifPresent(shopTag -> {
            if (!shopTag.getShopId().equals(shopId)) {
                throw new ShopException(ShopErrorCode.SHOP_TAG_MISMATCH);
            }
        });

        shopMenuTagRepository.deleteByShopMenuIdAndAnyTagId(menuId, tagId);
    }

    private void validateOwner(Long shopId, Long ownerUserId) {
        if (!shopReader.isShopOwnedBy(shopId, ownerUserId)) {
            throw new ShopException(ShopErrorCode.SHOP_OWNER_MISMATCH);
        }
    }

    private ShopMenu readMenuInShop(Long shopId, Long menuId) {
        try {
            return shopMenuReader.getByIdAndShopId(shopId, menuId);
        } catch (RuntimeException e) {
            throw new ShopException(ShopErrorCode.SHOP_MENU_MISMATCH);
        }
    }

    private ResolvedTagIds resolveTagIds(Long shopId, Long requestedTagId) {
        return shopTagRepository.findById(requestedTagId)
                .map(shopTag -> resolveShopTagIds(shopId, shopTag))
                .orElseGet(() -> resolveLegacyTagIds(shopId, requestedTagId));
    }

    private ResolvedTagIds resolveShopTagIds(Long shopId, ShopTag shopTag) {
        if (!shopTag.getShopId().equals(shopId)) {
            throw new ShopException(ShopErrorCode.SHOP_TAG_MISMATCH);
        }

        Tag legacyTag = tagRepository.findByName(shopTag.getName())
                .orElseGet(() -> tagRepository.save(Tag.create(shopTag.getName())));

        return new ResolvedTagIds(legacyTag.getId(), shopTag.getId());
    }

    private ResolvedTagIds resolveLegacyTagIds(Long shopId, Long requestedTagId) {
        Tag legacyTag = tagRepository.findById(requestedTagId)
                .orElseThrow(() -> new ShopException(ShopErrorCode.SHOP_TAG_MISMATCH));

        ShopTag shopTag = shopTagRepository.findByShopIdAndName(shopId, legacyTag.getName())
                .orElseGet(() -> {
                    int nextSortOrder = shopTagRepository.findMaxSortOrderByShopId(shopId) + 1;
                    return shopTagRepository.save(ShopTag.create(shopId, legacyTag.getName(), nextSortOrder));
                });

        return new ResolvedTagIds(legacyTag.getId(), shopTag.getId());
    }

    private record ResolvedTagIds(Long legacyTagId, Long shopTagId) {
    }
}
