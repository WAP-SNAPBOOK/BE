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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final ShopReader shopReader;
    private final ShopMenuReader shopMenuReader;
    private final ShopTagRepository shopTagRepository;
    private final ShopMenuTagRepository shopMenuTagRepository;

    @Deprecated(forRemoval = false)
    @Transactional
    public TagResponse createOrGet(String name) {
        log.warn("legacy global tag createOrGet used name={}", name);
        Tag tag = tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(Tag.create(name)));
        return new TagResponse(tag);
    }

    @Transactional
    public TagResponse createShopTag(Long shopId, String name) {
        shopTagRepository.findByShopIdAndName(shopId, name)
                .ifPresent(existingTag -> {
                    throw new ShopException(ShopErrorCode.SHOP_TAG_ALREADY_EXISTS);
                });

        int nextSortOrder = shopTagRepository.findMaxSortOrderByShopId(shopId) + 1;
        ShopTag tag = shopTagRepository.save(ShopTag.create(shopId, name, nextSortOrder));
        return new TagResponse(tag);
    }

    @Transactional
    public TagResponse createShopTag(Long shopId, Long ownerUserId, String name) {
        validateOwner(shopId, ownerUserId);
        return createShopTag(shopId, name);
    }

    @Deprecated(forRemoval = false)
    public List<TagResponse> getAllTags() {
        log.warn("legacy global tag list used");
        return tagRepository.findAll().stream()
                .map(TagResponse::new)
                .toList();
    }

    public List<TagResponse> getVisibleShopTags(Long shopId) {
        return readVisibleShopTags(shopId).stream()
                .map(TagResponse::new)
                .toList();
    }

    public List<TagResponse> getManageShopTags(Long shopId, Long ownerUserId) {
        validateOwner(shopId, ownerUserId);

        return shopTagRepository.findByShopIdOrderBySortOrderAsc(shopId).stream()
                .map(TagResponse::new)
                .toList();
    }

    @Transactional
    public TagResponse updateShopTag(Long shopId, Long tagId, Long ownerUserId, String name) {
        validateOwner(shopId, ownerUserId);

        ShopTag tag = readShopTagInShop(shopId, tagId);
        shopTagRepository.findByShopIdAndName(shopId, name)
                .filter(existingTag -> !existingTag.getId().equals(tagId))
                .ifPresent(existingTag -> {
                    throw new ShopException(ShopErrorCode.SHOP_TAG_ALREADY_EXISTS);
                });

        tag.updateName(name);
        return new TagResponse(tag);
    }

    @Transactional
    public void deleteShopTag(Long shopId, Long tagId, Long ownerUserId) {
        validateOwner(shopId, ownerUserId);

        ShopTag tag = readShopTagInShop(shopId, tagId);
        shopMenuTagRepository.deleteByShopTagId(tag.getId());
        shopTagRepository.delete(tag);
    }

    @Transactional
    public void updateShopTagOrder(Long shopId, Long ownerUserId, List<Long> requestedVisibleTagIds) {
        validateOwner(shopId, ownerUserId);

        List<ShopTag> visibleTags = readVisibleShopTags(shopId);
        validateRequestedOrder(visibleTags, requestedVisibleTagIds);

        List<ShopTag> allTags = shopTagRepository.findByShopIdOrderBySortOrderAsc(shopId);
        Set<Long> visibleTagIdSet = new LinkedHashSet<>(requestedVisibleTagIds);

        List<Long> canonicalOrder = new java.util.ArrayList<>(requestedVisibleTagIds);
        allTags.stream()
                .map(ShopTag::getId)
                .filter(tagId -> !visibleTagIdSet.contains(tagId))
                .forEach(canonicalOrder::add);

        shopTagRepository.shiftSortOrders(shopId, allTags.size());
        for (int i = 0; i < canonicalOrder.size(); i++) {
            shopTagRepository.updateSortOrder(shopId, canonicalOrder.get(i), i);
        }
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

    private ShopTag readShopTagInShop(Long shopId, Long tagId) {
        return shopTagRepository.findByIdAndShopId(tagId, shopId)
                .orElseThrow(() -> new ShopException(ShopErrorCode.SHOP_TAG_MISMATCH));
    }

    private List<ShopTag> readVisibleShopTags(Long shopId) {
        Map<Long, ShopTag> merged = java.util.stream.Stream.concat(
                        shopTagRepository.findVisibleByShopTagIdOrderBySortOrderAsc(shopId).stream(),
                        shopTagRepository.findVisibleByLegacyTagFallbackOrderBySortOrderAsc(shopId).stream()
                )
                .collect(Collectors.toMap(
                        ShopTag::getId,
                        Function.identity(),
                        (left, right) -> left
                ));

        return merged.values().stream()
                .sorted(Comparator.comparing(ShopTag::getSortOrder).thenComparing(ShopTag::getId))
                .toList();
    }

    private void validateRequestedOrder(List<ShopTag> visibleTags, List<Long> requestedVisibleTagIds) {
        if (requestedVisibleTagIds == null || requestedVisibleTagIds.isEmpty()) {
            throw new ShopException(ShopErrorCode.INVALID_SHOP_TAG_ORDER);
        }

        Set<Long> expectedVisibleTagIds = visibleTags.stream()
                .map(ShopTag::getId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<Long> requestedUniqueTagIds = new LinkedHashSet<>(requestedVisibleTagIds);

        if (requestedUniqueTagIds.size() != requestedVisibleTagIds.size()) {
            throw new ShopException(ShopErrorCode.INVALID_SHOP_TAG_ORDER, "중복 태그 ID는 허용되지 않습니다.");
        }

        if (expectedVisibleTagIds.size() != requestedVisibleTagIds.size()) {
            throw new ShopException(ShopErrorCode.INVALID_SHOP_TAG_ORDER, "visible 태그 전체를 전달해야 합니다.");
        }

        if (!expectedVisibleTagIds.equals(requestedUniqueTagIds)) {
            throw new ShopException(ShopErrorCode.INVALID_SHOP_TAG_ORDER, "visible 태그 집합이 현재 상태와 일치하지 않습니다.");
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
        log.warn("legacy tag_id fallback used shopId={} legacyTagId={}", shopId, requestedTagId);
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
